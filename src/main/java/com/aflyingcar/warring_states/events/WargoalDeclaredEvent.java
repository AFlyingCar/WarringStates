package com.aflyingcar.warring_states.events;

import com.aflyingcar.warring_states.api.IWarGoal;
import net.minecraftforge.fml.common.eventhandler.Event;

public class WargoalDeclaredEvent extends Event {
    private final IWarGoal warGoal;

    public WargoalDeclaredEvent(IWarGoal warGoal) {
        this.warGoal = warGoal;
    }

    public IWarGoal getWarGoal() {
        return warGoal;
    }
}
