package com.aflyingcar.warring_states.war.goals;

import com.aflyingcar.warring_states.WarringStatesConfig;
import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.war.Conflict;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class WaitoutTimerWarGoal implements IWarGoal {

    @Override
    public void update(float dt) {

    }

    @Override
    public boolean accomplished(Conflict war) {
        return war.getWarStartTimer().getNumberOfMinutes() >= WarringStatesConfig.defenderWaitVictoryMaxTime;
    }

    @Override
    public void onSuccess(State owner) { }

    @Override
    public boolean canBeDeclared(EntityPlayer declarer) {
        // Cannot be declared by anybody, is given automatically
        return false;
    }

    @Override
    public NBTTagCompound writeNBT(NBTTagCompound nbt) {
        nbt.setInteger("_id", WarGoalFactory.Goals.WAITOUT_TIMER.ordinal());
        return nbt;
    }

    @Override
    public void readNBT(NBTTagCompound nbt) { }

    @Override
    public void writeToBuf(ByteBuf buf) {
        buf.writeInt(WarGoalFactory.Goals.WAITOUT_TIMER.ordinal());
    }

    @Override
    public void readFromBuf(ByteBuf buf) { }
}
