package com.aflyingcar.warring_states.war.goals;

import com.aflyingcar.warring_states.api.IWarGoal;
import net.minecraft.nbt.NBTTagCompound;

public class WarGoalFactory {
    public static IWarGoal newWargoal(NBTTagCompound tagCompound) {
        int _id = tagCompound.getInteger("_id");

        return newWargoal(Goals.fromInt(_id));
    }

    public static IWarGoal newWargoal(Goals goal) {
        switch(goal) {
            case STEAL_CHUNK:
                return new StealChunkWarGoal();
            case WAITOUT_TIMER:
                return new WaitoutTimerWarGoal();
            case INVALID:
            default:
                return null;
        }
    }

    public enum Goals {
        INVALID,
        STEAL_CHUNK,
        WAITOUT_TIMER;

        private final static Goals[] values = Goals.values();

        public static Goals fromInt(int id) {
            return (id > 0 && id < values.length) ? values[id] : INVALID;
        }
    }
}
