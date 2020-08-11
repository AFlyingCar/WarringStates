package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.TrackedMessage;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nonnull;
import java.util.UUID;

public class RequestAllValidWarrableStatesMessage extends TrackedMessage {
    private UUID stateID;
    private UUID playerID;

    public RequestAllValidWarrableStatesMessage() {
    }

    public RequestAllValidWarrableStatesMessage(UUID stateID, UUID playerID) {
        this.stateID = stateID;
        this.playerID = playerID;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        stateID = NetworkUtils.readUUID(buf);
        playerID = NetworkUtils.readUUID(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        NetworkUtils.writeUUID(buf, stateID);
        NetworkUtils.writeUUID(buf, playerID);
    }

    @Nonnull
    public UUID getStateID() {
        return stateID;
    }

    @Nonnull
    public UUID getPlayerID() {
        return playerID;
    }
}
