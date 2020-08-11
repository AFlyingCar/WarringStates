package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.TrackedMessage;
import io.netty.buffer.ByteBuf;

import java.util.Map;
import java.util.UUID;

public class DeliverStateCitizenApplicationListMessage extends TrackedMessage {
    private Map<UUID, String> applications;

    public DeliverStateCitizenApplicationListMessage() {
    }

    public DeliverStateCitizenApplicationListMessage(Map<UUID, String> applications) {
        this.applications = applications;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        applications = NetworkUtils.readMap(buf, NetworkUtils::readUUID, NetworkUtils::readString);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        NetworkUtils.writeMap(buf, applications, NetworkUtils::writeUUID, NetworkUtils::writeString);
    }

    public Map<UUID, String> getCitizenApplications() {
        return applications;
    }
}
