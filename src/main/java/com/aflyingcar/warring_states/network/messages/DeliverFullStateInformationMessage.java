package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.states.DummyState;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.util.TrackedMessage;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nonnull;

public class DeliverFullStateInformationMessage extends TrackedMessage {
    private DummyState state;

    public DeliverFullStateInformationMessage() {
    }

    public DeliverFullStateInformationMessage(@Nonnull State state) {
        this.state = new DummyState(state);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        state = DummyState.readStateData(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        state.writeData(buf);
    }

    public DummyState getState() {
        return state;
    }
}
