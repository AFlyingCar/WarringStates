package com.aflyingcar.warring_states.events;

import com.aflyingcar.warring_states.war.Conflict;
import com.aflyingcar.warring_states.war.DummyConflict;
import net.minecraftforge.fml.common.eventhandler.Event;

public class WarDeclaredEvent extends Event {
    private final Conflict war;

    public WarDeclaredEvent(Conflict war) {
        this.war = war;
    }

    public Conflict getWar() {
        return war;
    }

    public static class CLIENT extends Event {
        private final DummyConflict war;

        public CLIENT(DummyConflict war) {
            this.war = war;
        }

        public DummyConflict getWar() {
            return war;
        }
    }
}
