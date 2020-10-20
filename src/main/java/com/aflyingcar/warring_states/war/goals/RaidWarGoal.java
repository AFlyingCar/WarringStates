package com.aflyingcar.warring_states.war.goals;

import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.util.NBTUtils;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.PlayerUtils;
import com.aflyingcar.warring_states.war.Conflict;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class RaidWarGoal implements IWarGoal {
    List<UUID> deadPlayers = new ArrayList<>();
    List<UUID> livingPlayers = new ArrayList<>();
    Conflict.Side side = Conflict.Side.WAR_NOT_OVER;

    @Override
    public void onWarStarted(Conflict war, State owner) {
        side = war.getSideOf(owner);

        switch(Objects.requireNonNull(side)) {
            case BELLIGERENT:
                livingPlayers = war.getDefenders().keySet().stream().map(State::getCitizens).flatMap(Collection::stream).filter(PlayerUtils::isPlayerOnline).collect(Collectors.toList());
                break;
            case DEFENDER:
                livingPlayers = war.getBelligerents().keySet().stream().map(State::getCitizens).flatMap(Collection::stream).filter(PlayerUtils::isPlayerOnline).collect(Collectors.toList());
                break;
            default:
        }
    }

    @Override
    public void update(float dt) { }

    @Override
    public boolean accomplished(Conflict war) {
        return livingPlayers.isEmpty();
    }

    @Override
    public void onSuccess(State owner) { }

    @Override
    public void onPlayerLogin(EntityPlayer player, Conflict.Side side) {
        // Do not re-add players to the list if they have died
        if(deadPlayers.contains(player.getPersistentID())) return;

        if(side != this.side) {
            livingPlayers.add(player.getPersistentID());
        }
    }

    @Override
    public void onPlayerLogout(EntityPlayer player, Conflict.Side side) {
        // If the player logs out, remove them from the list of living players but don't add them to the list of dead players so they can log back in
        if(side != this.side) {
            livingPlayers.remove(player.getPersistentID());
        }
    }

    @Override
    public void onPlayerDeath(EntityPlayer player, Conflict.Side side) {
        if(side != this.side) {
            livingPlayers.remove(player.getPersistentID());
            deadPlayers.add(player.getPersistentID());
        }
    }

    @Override
    public boolean canBeDeclared(EntityPlayer declarer) {
        return true;
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        NetworkUtils.writeCollection(buf, livingPlayers, NetworkUtils::writeUUID);
        // NetworkUtils.writeCollection(buf, deadPlayers, NetworkUtils::writeUUID);
        buf.writeInt(side.ordinal());
    }

    @Override
    public void readFromBuf(ByteBuf buf) {
        livingPlayers = NetworkUtils.readList(buf, NetworkUtils::readUUID);
        // deadPlayers = NetworkUtils.readList(buf, NetworkUtils::readUUID);
        side = Conflict.Side.values()[buf.readInt()];
    }

    @Override
    public NBTTagCompound writeNBT(NBTTagCompound nbt) {
        nbt.setTag("livingPlayers", NBTUtils.serializeCollection(livingPlayers, NBTUtils::serializeUUID));
        nbt.setTag("deadPlayers", NBTUtils.serializeCollection(deadPlayers, NBTUtils::serializeUUID));
        nbt.setInteger("side", side.ordinal());

        return nbt;
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        livingPlayers = NBTUtils.deserializeGenericList(nbt.getTagList("livingPlayers", 10), nbtBase -> NBTUtils.deserializeUUID((NBTTagCompound) nbtBase));
        deadPlayers = NBTUtils.deserializeGenericList(nbt.getTagList("deadPlayers", 10), nbtBase -> NBTUtils.deserializeUUID((NBTTagCompound) nbtBase));
        side = Conflict.Side.values()[nbt.getInteger("side")];
    }

    @Nonnull
    @Override
    public IWarGoal createOpposingWargoal() {
        return new RaidWarGoal();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        RaidWarGoal other = (RaidWarGoal) o;
        return side == other.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(side);
    }
}
