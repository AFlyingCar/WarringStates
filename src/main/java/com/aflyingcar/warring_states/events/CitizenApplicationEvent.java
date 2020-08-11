package com.aflyingcar.warring_states.events;

import com.aflyingcar.warring_states.states.State;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.UUID;

/**
 * Event that notifies of a player applying for Citizenship in a State
 *
 * Will be fired both client-side and server-side
 * Client-Side: Will be fired before {@code UpdateStateInfoMessage} is sent to the server
 * Server-side: Will be fired before {@code State#apply()} is called.
 *   -> Canceling the event on the Server will result in {@code State#apply()} not being called.
 */
public class CitizenApplicationEvent extends Event {
    private final State state;
    private final UUID applyingUUID;

    public CitizenApplicationEvent(State state, UUID applyingUUID) {
        this.state = state;
        this.applyingUUID = applyingUUID;
    }

    public UUID getApplyingUUID() {
        return applyingUUID;
    }

    public State getState() {
        return state;
    }
}
