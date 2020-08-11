package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.states.DummyState;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.TrackedMessage;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DeliverAllValidWarrableStatesMessage extends TrackedMessage {
    private Map<DummyState, Integer> wargoals;

    public DeliverAllValidWarrableStatesMessage() {
    }

    public DeliverAllValidWarrableStatesMessage(Map<UUID, Set<IWarGoal>> wargoals) {
        this.wargoals = new HashMap<>();
        for(Map.Entry<UUID, Set<IWarGoal>> entry : wargoals.entrySet()) {
            State s = StateManager.getInstance().getStateFromUUID(entry.getKey());
            if(s != null) {
                this.wargoals.put(new DummyState(s), entry.getValue().size());
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        wargoals = NetworkUtils.readMap(buf, byteBuf -> new DummyState(NetworkUtils.readUUID(byteBuf), NetworkUtils.readString(byteBuf), NetworkUtils.readString(byteBuf)), ByteBuf::readInt);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        NetworkUtils.writeMap(buf, wargoals, (byteBuf, dummyState) -> {
            NetworkUtils.writeUUID(byteBuf, dummyState.getUUID());
            NetworkUtils.writeString(byteBuf, dummyState.getName());
            NetworkUtils.writeString(byteBuf, dummyState.getDesc());
        }, ByteBuf::writeInt);
    }

    public Map<DummyState, Integer> getWargoals() {
        return wargoals;
    }
}
