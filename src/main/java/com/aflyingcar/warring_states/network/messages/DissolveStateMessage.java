package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.util.NetworkUtils;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import javax.annotation.Nonnull;
import java.util.UUID;

public class DissolveStateMessage implements IMessage {
    private UUID stateID;
    private UUID playerID;

    public DissolveStateMessage() {
    }

    public DissolveStateMessage(@Nonnull UUID stateID, @Nonnull UUID playerID) {
        this.stateID = stateID;
        this.playerID = playerID;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        stateID = NetworkUtils.readUUID(buf);
        playerID = NetworkUtils.readUUID(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkUtils.writeUUID(buf, stateID);
        NetworkUtils.writeUUID(buf, playerID);
    }

    @Nonnull
    public UUID getPlayerID() {
        return playerID;
    }

    @Nonnull
    public UUID getStateID() {
        return stateID;
    }
}
