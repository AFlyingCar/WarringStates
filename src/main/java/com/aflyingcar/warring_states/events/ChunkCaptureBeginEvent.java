package com.aflyingcar.warring_states.events;

import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import com.aflyingcar.warring_states.war.Conflict;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ChunkCaptureBeginEvent extends Event {
    private final TileEntityClaimer claimer;
    private final Conflict conflict;
    private final State capturerState;

    public ChunkCaptureBeginEvent(TileEntityClaimer claimer, Conflict conflict, State capturerState) {
        this.claimer = claimer;
        this.conflict = conflict;
        this.capturerState = capturerState;
    }

    public TileEntityClaimer getClaimer() {
        return claimer;
    }

    public Conflict getConflict() {
        return conflict;
    }

    public State getCapturerState() {
        return capturerState;
    }
}
