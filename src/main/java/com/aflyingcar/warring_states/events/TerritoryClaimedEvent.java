package com.aflyingcar.warring_states.events;

import com.aflyingcar.warring_states.states.State;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

public class TerritoryClaimedEvent extends Event {
    private final State state;
    private final World world;
    private final BlockPos pos;

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
}
