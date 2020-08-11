package com.aflyingcar.warring_states.events;

import com.aflyingcar.warring_states.war.Conflict;
import net.minecraftforge.fml.common.eventhandler.Event;

public class WarEndsEvent extends Event {
    private final Conflict war;

    public WarEndsEvent(Conflict war) {
        this.war = war;
    }

    public Conflict getConflict() {
        return war;
    }
}
