package com.aflyingcar.warring_states.events;

import com.aflyingcar.warring_states.war.Conflict;
import net.minecraftforge.fml.common.eventhandler.Event;

public class WarCompleteEvent extends Event {
    private final Conflict war;

    public WarCompleteEvent(Conflict war) {
        this.war = war;
    }

    public Conflict getWar() {
        return war;
    }
}
