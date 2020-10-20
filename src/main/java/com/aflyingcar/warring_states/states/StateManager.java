package com.aflyingcar.warring_states.states;

import com.aflyingcar.warring_states.WarringStatesConfig;
import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.util.BaseManager;
import com.aflyingcar.warring_states.util.WorldUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A manager for states. Only accessible on the server-side
 */
public final class StateManager extends BaseManager {
    public static final String DATA_NAME = WarringStatesMod.MOD_ID + "_STATEDATA";

    private List<State> states;

    private static StateManager instance;

    private StateManager() {
        resetAllData();
    }

    public static StateManager getInstance() {
        if(instance == null) {
            instance = new StateManager();
        }

        return instance;
    }

    @Override
    protected File getDataFile(File rootGameDir) {
        return new File(rootGameDir, DATA_NAME + ".dat");
    }

    @Override
    protected void readFromNBT(NBTTagCompound nbt) {
        if(nbt.hasKey("states")) {
            NBTTagList nbtStates = nbt.getTagList("states", 10); // COMPOUND

            for(NBTBase nbtState : nbtStates) {
                State state = new State();
                state.readNBT((NBTTagCompound) nbtState);

                if(WarringStatesConfig.performTerritorySanityCheckOnStartup) {
                    state.sanityCheckAllClaimedTerritory(WarringStatesConfig.shouldProblemsBeFixedDuringSanityCheck);
                }

                states.add(state);
            }
        }
    }

    @Override
    protected NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList nbtStates = new NBTTagList();

        for(State state : states) {
            NBTTagCompound nbtState = new NBTTagCompound();

            nbtStates.appendTag(state.writeNBT(nbtState));
        }
        compound.setTag("states", nbtStates);

        return compound;
    }

    public List<State> getStates() {
        return states;
    }

    /**
     * Gets the state that owns the given block position
     * @param world The World the block is in
     * @param pos The position of the block to check
     * @return The state that owns the given block, or null if it is unowned
     */
    @Nullable
    public State getStateAtPosition(World world, BlockPos pos) {
        Chunk c = world.getChunk(pos);

        return getStateAtPosition(c.getPos(), WorldUtils.getDimensionIDForWorld(world));
    }

    @Nullable
    public State getStateAtPosition(ChunkPos position, int dimension) {
        for(State s : states) {
            if(s.doesControlTerritory(position, dimension)) {
                return s;
            }
        }

        return null;
    }

    /**
     * Gets the State that the player is a citizen of
     * @param player The player
     * @return The state that {@code player} is a citizen of, or null if they are not a citizen of any state
     */
    @Nullable
    public State getStateFromPlayer(EntityPlayer player) {
        return getStateFromPlayerUUID(player.getUniqueID());
    }

    /**
     * Gets the state that the player's UUID is a citizen of
     * @param uuid The UUID of a player
     * @return The state that {@code uuid} is a citizen of, or null if they are not a citizen of any state
     */
    @Nullable
    public State getStateFromPlayerUUID(UUID uuid) {
        for(State state : states) {
            if(state.hasCitizen(uuid))
                return state;
        }
        return null;
    }

    /**
     * Gets a state by its name
     * TODO: This should be changed to instead work by UUID instead of name
     * @param name The name of the state
     * @return The state to get, or null if there is no such state by that name
     */
    @Nullable
    public State getStateFromName(String name) {
        for(State state : states) {
            if(state.getName().equals(name))
                return state;
        }

        return null;
    }

    public State newState(String name, String description, UUID stateID, @Nonnull EntityPlayer founder) {
        return newState(name, description, stateID, founder.getPersistentID());
    }

    public State newState(@Nonnull String name, String description, @Nonnull UUID stateID, @Nonnull UUID founder) {
        return newState(new State(name, description, stateID, founder));
    }

    private State newState(@Nonnull State state) {
        if(!states.contains(state)) {
            state.onStateFounded();
            states.add(state);
            markDirty();
        } else {
            WarringStatesMod.getLogger().error("Cannot add a state with a name that already exists!");
        }

        return state;
    }

    /**
     * Resets all State Manager data to what it would look like on startup.
     * Note that the dirty flag is not checked, and care must be taken to ensure that all important data is saved _before_
     *  this gets called.
     */
    public void resetAllData() {
        states = new ArrayList<>();

        setDirty(false);
        setRootGameDirectory(null);
    }

    @Override
    public void update() {
        for(State s : states) {
            // Make sure the minimum amount of time has passed before decaying a claim
            if(s.getDecayTimer().getNumberOfDays() > WarringStatesConfig.numberOfDaysBeforeClaimDecayBegins) {
                long hoursSinceDecayBegan = s.getDecayTimer().getNumberOfHours() - (WarringStatesConfig.numberOfDaysBeforeClaimDecayBegins * 24);

                // Make sure to only decay a chunk ONCE every numberOfHoursBetweenEachChunkDecay hours
                if(hoursSinceDecayBegan % WarringStatesConfig.numberOfHoursBetweenEachChunkDecay == 0 && !s.hasDecayed()) {
                    s.decayFurthestClaim();
                } else {
                    // It is now past the correct hour to perform a decay, so mark that we haven't decayed yet for the next time
                    s.setHasDecayed(false);
                }
                markDirty();
            }
        }
    }

    @Nullable
    public State getStateFromUUID(UUID stateID) {
        for(State s : states) {
            if(s.getUUID().equals(stateID)) {
                return s;
            }
        }

        return null;
    }

    public boolean hasPendingApplication(@Nonnull UUID playerUUID) {
        return states.stream().anyMatch(s -> s.hasApplicationFor(playerUUID));
    }

    public int getPrivilegesFor(EntityPlayer player) {
        return getPrivilegesFor(player.getPersistentID());
    }

    public int getPrivilegesFor(UUID playerUUID) {
        return states.stream().filter(s -> s.hasCitizen(playerUUID)).findFirst().map(s -> s.getPrivileges(playerUUID)).orElse(0);
    }

    public void removeState(State state) {
        states.remove(state);
        markDirty();
    }

    public void removeAllApplicationsFor(UUID playerUUID) {
        states.stream().filter(s -> s.hasApplicationFor(playerUUID)).forEach(s -> s.rejectApplicationFor(playerUUID));
    }
}
