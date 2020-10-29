package com.aflyingcar.warring_states.events;

import com.aflyingcar.warring_states.states.State;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

public class TerritoryClaimedEvent extends Event {
    private final State state;
    private final World world;
    private final BlockPos pos;

    private String reasonKey;
    private Object[] reasonArgs;

    public TerritoryClaimedEvent(State state, World world, BlockPos position) {
        this.state = state;
        this.world = world;
        this.pos = position;
    }

    public State getState() {
        return state;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return  pos;
    }

    public void setFailureKey(String reason) {
        this.reasonKey = reason;
    }

    public void setFailureArguments(Object... args) {
        this.reasonArgs = args;
    }

    public TextComponentBase getFailureReason() {
        return new TextComponentTranslation(reasonKey, reasonArgs);
    }
}
