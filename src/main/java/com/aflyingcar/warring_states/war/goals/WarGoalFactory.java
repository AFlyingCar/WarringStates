package com.aflyingcar.warring_states.war.goals;

import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.util.NetworkUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class WarGoalFactory {
    public static IWarGoal newWargoal(NBTTagCompound tagCompound) {
        String _id = tagCompound.getString("_id");

        IWarGoal goal = newWargoal(Goals.valueOf(_id));
        if(goal != null) {
            goal.readNBT(tagCompound);
        }
        return goal;
    }

    public static IWarGoal newWargoal(ByteBuf buf) {
        String _id = NetworkUtils.readString(buf);

        IWarGoal goal = newWargoal(Goals.valueOf(_id));

        if(goal != null) {
            goal.readFromBuf(buf);
        }

        return goal;
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
        INVALID(false),
        STEAL_CHUNK(true),
        RAID(true),

        // UNCLAIMABLE WARGOALS

        WAITOUT_TIMER(false),
        ;

        private boolean canBeDeclared;

        Goals(boolean canBeDeclared) {
            this.canBeDeclared = canBeDeclared;
        }

        public boolean canBeDeclared() {
            return canBeDeclared;
        }

        public Goals next() {
            return fromInt(ordinal() + 1);
        }

        private final static Goals[] values = Goals.values();

        public static Goals fromInt(int id) {
            return (id > 0 && id < values.length) ? values[id] : INVALID;
        }
    }
}
