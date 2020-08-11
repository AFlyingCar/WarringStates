package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.states.DummyCitizen;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.TrackedMessage;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class DeliverStateCitizenListMessage extends TrackedMessage {
    private List<DummyCitizen> citizenList;

    public DeliverStateCitizenListMessage() { }

    public DeliverStateCitizenListMessage(@Nonnull Map<UUID, Integer> citizenNamePrivilegeMapping) {
        citizenList = citizenNamePrivilegeMapping.entrySet().stream().map(e -> new DummyCitizen(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        citizenList = NetworkUtils.readList(buf, byteBuf -> new DummyCitizen(NetworkUtils.readUUID(byteBuf), byteBuf.readInt(), NetworkUtils.readString(byteBuf)));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        NetworkUtils.writeList(buf, citizenList, (byteBuf, dummyCitizen) -> {
            NetworkUtils.writeUUID(byteBuf, dummyCitizen.getCitizenID());
            byteBuf.writeInt(dummyCitizen.getPrivileges());
            NetworkUtils.writeString(byteBuf, dummyCitizen.getName());
        });
    }

    @Nonnull
    public Map<String, Integer> getCitizenNamePrivilegeMapping() {
        return citizenList.stream().collect(Collectors.toMap(DummyCitizen::getName, DummyCitizen::getPrivileges));
    }

    @Nonnull
    public List<DummyCitizen> getCitizenList() {
        return citizenList;
    }
}
