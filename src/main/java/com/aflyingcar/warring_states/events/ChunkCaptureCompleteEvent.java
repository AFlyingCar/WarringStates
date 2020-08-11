package com.aflyingcar.warring_states.events;

import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;

public class ChunkCaptureCompleteEvent extends Event {
    final TileEntityClaimer capturedClaimer;

    public ChunkCaptureCompleteEvent(@Nonnull TileEntityClaimer capturedClaimer) {
        this.capturedClaimer = capturedClaimer;
    }

    @Nonnull
    public TileEntityClaimer getCapturedClaimer() {
        return capturedClaimer;
    }
}
