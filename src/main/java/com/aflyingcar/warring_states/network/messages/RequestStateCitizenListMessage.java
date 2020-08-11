package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.TrackedMessage;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class RequestStateCitizenListMessage extends TrackedMessage {
    private UUID stateID;

    public RequestStateCitizenListMessage() {
    }

    public RequestStateCitizenListMessage(UUID stateID) {
        this.stateID = stateID;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        stateID = NetworkUtils.readUUID(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        NetworkUtils.writeUUID(buf, stateID);
    }

    public UUID getStateID() {
        return stateID;
    }
}
