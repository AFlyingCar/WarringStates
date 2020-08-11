package com.aflyingcar.warring_states.events;

import com.aflyingcar.warring_states.war.Conflict;
import net.minecraftforge.fml.common.eventhandler.Event;

public class WarDeclaredEvent extends Event {
    private final Conflict war;

    public WarDeclaredEvent(Conflict war) {
        this.war = war;
    }

    public Conflict getWar() {
        return war;
    }
}
