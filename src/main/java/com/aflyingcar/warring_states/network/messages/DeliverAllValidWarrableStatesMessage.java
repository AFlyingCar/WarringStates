package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.TrackedMessage;
import com.aflyingcar.warring_states.util.WarrableState;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class DeliverAllValidWarrableStatesMessage extends TrackedMessage {
    private List<WarrableState> warrableStates;

    public DeliverAllValidWarrableStatesMessage() {
    }

    public DeliverAllValidWarrableStatesMessage(List<WarrableState> warrableStates) {
        this.warrableStates = warrableStates;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        warrableStates = NetworkUtils.readList(buf, WarrableState::createFromBuf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        NetworkUtils.writeCollection(buf, warrableStates, NetworkUtils::writeNetSerializable);
    }

    public List<WarrableState> getWarrableStates() {
        return warrableStates;
    }
}
