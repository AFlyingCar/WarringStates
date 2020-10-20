package com.aflyingcar.warring_states.war.goals;

import com.aflyingcar.warring_states.WarringStatesConfig;
import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.WorldUtils;
import com.aflyingcar.warring_states.war.Conflict;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Set;

@ParametersAreNonnullByDefault
public class StealChunkWarGoal implements IWarGoal {
    public final static float MAX_PROGRESS = 200; // 100% enemy + 100% ours
    public final static float TIME_FOR_PERCENT = 60; // TODO: 1% for every second?

    private ChunkPos chunk;
    private int dimension;
    private boolean accomplished = false;

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public void setChunk(ChunkPos chunk) {
        this.chunk = chunk;
    }

    public ChunkPos getChunk() {
        return chunk;
    }

    public int getDimension() {
        return dimension;
    }

    public void accomplish() {
        accomplished = true;
    }

    @Override
    public void onWarStarted(Conflict war, State owner) { }

    @SideOnly(Side.SERVER)
    @Override
    public void update(float dt) { }

    @Override
    public boolean accomplished(Conflict war) {
        return accomplished;
    }

    @Override
    public void onSuccess(State owner) {
        State originalOwner = StateManager.getInstance().getStateAtPosition(chunk, dimension);

        if(originalOwner == null) {
            WarringStatesMod.getLogger().error("We have a war goal against a state, but no state seems to be at the position we are fighting over? What was the point then?");
            WarringStatesMod.getLogger().error("Not granting this wargoal, talk to AFlyingCar, something is wrong here!");
        } else {
            WarringStatesMod.getLogger().info("Removing control of chunk " + chunk + " from " + originalOwner.getName());
            originalOwner.unclaimTerritory(chunk, dimension);

            WarringStatesMod.getLogger().info("Granting control of chunk " + chunk + " to " + owner.getName());
            owner.claimTerritory(chunk, dimension);

            // Get this claimer and update its owner
            List<TileEntityClaimer> claimers = WorldUtils.getTileEntitiesWithinChunk(TileEntityClaimer.class, FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension), chunk);

            // This should never happen, but lets do a sanity check anyway
            if(claimers.isEmpty()) {
                WarringStatesMod.getLogger().error("No TileEntityClaimer found in chunk " + chunk + " even though it is owned! Something horrible has happened here.");
            } else {
                // There should never be more than 1, so we'll just get the first one
                claimers.get(0).changeOwner(owner);
            }
        }
    }

    private static boolean checkWargoalAgainstOtherWargoals(@Nullable Set<IWarGoal> wargoals, ChunkPos declarerChunk) {
        return wargoals == null ||
               wargoals.stream().filter(StealChunkWarGoal.class::isInstance).map(StealChunkWarGoal.class::cast).map(StealChunkWarGoal::getChunk)
                                .anyMatch(c -> WorldUtils.areChunksAdjacent(c, declarerChunk));
    }

    public static boolean canWargoalBeDeclared(@Nullable EntityPlayer declarer) {
        if(declarer == null) return false;

        // Only perform this check on the server
        if(!declarer.world.isRemote) {
            ChunkPos declarerChunk = WorldUtils.getChunkFor(declarer.world, declarer.getPosition()).getPos();
            State declarerState = StateManager.getInstance().getStateFromPlayer(declarer);
            if(declarerState == null) return false;
            State target = StateManager.getInstance().getStateAtPosition(declarer.world, declarer.getPosition());
            if(target == null) return false;
            int dimension = WorldUtils.getDimensionIDForWorld(declarer.world);
            return checkWargoalAgainstOtherWargoals(declarerState.getWargoals().get(target.getUUID()), declarerChunk) ||
                   WorldUtils.doesChunkBorderWilderness(declarerChunk, dimension) || WorldUtils.doesChunkBorderState(declarerChunk, dimension);
        }

        return declarer.experience >= WarringStatesConfig.minimumExperienceRequiredForStealingChunks;
    }

    @Override
    public boolean canBeDeclared(EntityPlayer declarer) {
        return canWargoalBeDeclared(declarer);
    }

    @Nonnull
    @Override
    public IWarGoal createOpposingWargoal() {
        return new WaitoutTimerWarGoal();
    }

    @Override
    public NBTTagCompound writeNBT(NBTTagCompound nbt) {
        nbt.setInteger("_id", WarGoalFactory.Goals.STEAL_CHUNK.ordinal());
        nbt.setIntArray("chunk", new int[]{ chunk.x, chunk.z });
        nbt.setInteger("dimension", dimension);

        return nbt;
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        int[] xz = nbt.getIntArray("chunk");
        chunk = new ChunkPos(xz[0], xz[1]);

        dimension = nbt.getInteger("dimension");
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        buf.writeInt(WarGoalFactory.Goals.STEAL_CHUNK.ordinal());
        NetworkUtils.writeChunkPos(buf, chunk);
        buf.writeInt(dimension);
        buf.writeBoolean(accomplished);
    }

    @Override
    public void readFromBuf(ByteBuf buf) {
        chunk = NetworkUtils.readChunkPos(buf);
        dimension = buf.readInt();
        accomplished = buf.readBoolean();
    }
}
