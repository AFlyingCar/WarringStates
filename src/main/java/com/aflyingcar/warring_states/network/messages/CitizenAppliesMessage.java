package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.util.NetworkUtils;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import javax.annotation.Nonnull;
import java.util.UUID;

public class CitizenAppliesMessage implements IMessage {
    private UUID uuid;
    private UUID stateID;

    public CitizenAppliesMessage() { }
    public CitizenAppliesMessage(@Nonnull UUID newCitizenUUID, @Nonnull UUID stateID) {
        this.uuid = newCitizenUUID;
        this.stateID = stateID;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        uuid = NetworkUtils.readUUID(buf);
        stateID = NetworkUtils.readUUID(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkUtils.writeUUID(buf, uuid);
        NetworkUtils.writeUUID(buf, stateID);
    }

    public UUID getCitizenUUID() {
        return uuid;
    }

    public UUID getStateID() {
        return stateID;
    }
}
