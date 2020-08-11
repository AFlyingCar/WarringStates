package com.aflyingcar.warring_states.war;

import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.states.DummyState;
import com.aflyingcar.warring_states.states.State;
import net.minecraft.util.NonNullList;

import java.util.HashMap;
import java.util.Map;

public class DummyConflict {
    final Map<DummyState, Integer> belligerents;
    final Map<DummyState, Integer> defenders;

    public DummyConflict(Conflict conflict) {
        belligerents = new HashMap<>();
        defenders = new HashMap<>();

        for(Map.Entry<State, NonNullList<IWarGoal>> belligerent : conflict.getBelligerents().entrySet()) {
            belligerents.put(new DummyState(belligerent.getKey()), belligerent.getValue().size());
        }
        for(Map.Entry<State, NonNullList<IWarGoal>> defender : conflict.getDefenders().entrySet()) {
            defenders.put(new DummyState(defender.getKey()), defender.getValue().size());
        }
    }

    public DummyConflict(Map<DummyState, Integer> belligerents, Map<DummyState, Integer> defenders) {
        this.belligerents = belligerents;
        this.defenders = defenders;
    }

    public Map<DummyState, Integer> getBelligerents() {
        return belligerents;
    }

    public Map<DummyState, Integer> getDefenders() {
        return defenders;
    }
}
