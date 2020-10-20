package com.aflyingcar.warring_states.war.goals;

import com.aflyingcar.warring_states.api.IWarGoal;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class WarGoalFactory {
    public static IWarGoal newWargoal(NBTTagCompound tagCompound) {
        int _id = tagCompound.getInteger("_id");

        IWarGoal goal = newWargoal(_id);
        if(goal != null) {
            goal.readNBT(tagCompound);
        }
        return goal;
    }

    public static IWarGoal newWargoal(ByteBuf buf) {
        int _id = buf.readInt();

        IWarGoal goal = newWargoal(_id);

        if(goal != null) {
            goal.readFromBuf(buf);
        }

        return goal;
    }

    public static IWarGoal newWargoal(int id) {
        return newWargoal(Goals.fromInt(id));
    }

    public static IWarGoal newWargoal(Goals goal) {
        switch(goal) {
            case STEAL_CHUNK:
                return new StealChunkWarGoal();
            case WAITOUT_TIMER:
                return new WaitoutTimerWarGoal();
            case RAID:
                return new RaidWarGoal();
            case INVALID:
            default:
                return null;
        }
    }

    // TODO: This needs to be replaced with a more extensible solution
    public enum Goals {
        INVALID,
        STEAL_CHUNK,
        RAID,

        // UNCLAIMABLE WARGOALS

        WAITOUT_TIMER,
        ;

        private final static Goals[] values = Goals.values();

        public static Goals fromInt(int id) {
            return (id > 0 && id < values.length) ? values[id] : INVALID;
        }
    }
}
