package com.aflyingcar.warring_states.events;

import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.war.Conflict;

public class StateCapitulatesEvent {
    private final Conflict.Side side;
    private final State state;

    public StateCapitulatesEvent(State state, Conflict conflict) {
        this.state = state;

        this.side = conflict.getSideOf(state);
    }

    public State getState() {
        return state;
    }

    public Conflict.Side getSide() {
        return side;
    }
}
