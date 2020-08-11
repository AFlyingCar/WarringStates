package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.states.DummyCitizen;
import com.aflyingcar.warring_states.util.NetworkUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import javax.annotation.Nonnull;
import java.util.UUID;

public class UpdatePlayerPrivilegesMessage implements IMessage {
    private UUID stateID;
    private UUID playerID;
    private DummyCitizen citizen;

    public UpdatePlayerPrivilegesMessage() {
    }

    public UpdatePlayerPrivilegesMessage(UUID stateID, EntityPlayer player, DummyCitizen citizen) {
        this.stateID = stateID;
        this.playerID = player.getPersistentID();
        this.citizen = citizen;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        stateID = NetworkUtils.readUUID(buf);
        playerID = NetworkUtils.readUUID(buf);
        citizen = new DummyCitizen(NetworkUtils.readUUID(buf), buf.readInt(), NetworkUtils.readString(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkUtils.writeUUID(buf, stateID);
        NetworkUtils.writeUUID(buf, playerID);
        NetworkUtils.writeUUID(buf, citizen.getCitizenID());
        buf.writeInt(citizen.getPrivileges());
        NetworkUtils.writeString(buf, citizen.getName());
    }

    @Nonnull
    public UUID getPlayerID() {
        return playerID;
    }

    @Nonnull
    public UUID getStateID() {
        return stateID;
    }

    @Nonnull
    public DummyCitizen getCitizen() {
        return citizen;
    }
}
