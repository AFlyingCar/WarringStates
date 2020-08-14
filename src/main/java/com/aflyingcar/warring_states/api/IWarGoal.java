package com.aflyingcar.warring_states.api;

import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.util.ISerializable;
import com.aflyingcar.warring_states.war.Conflict;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Base class for a war goal
 */
@ParametersAreNonnullByDefault
public interface IWarGoal extends ISerializable {
    /**
     * Runs updates for this wargoal
     * @param dt Amount of time since the last update
     */
    @SuppressWarnings("EmptyMethod")
    void update(float dt);

    /**
     * Returns true if this goal has been accomplished
     * @param war The Conflict this wargoal is a part of
     * @return true if this wargoal has been accomplished
     */
    boolean accomplished(Conflict war);

    /**
     * Called when the conflict this wargoal is used in ends and the owner of this wargoal was one of the winners
     * @param owner The State which owns this wargoal
     */
    void onSuccess(State owner);

    /**
     * Checks if this wargoal can be declared by this person.
     * @param declarer The player attempting to declare the war
     * @return True if this wargoal can be declared, false otherwise.
     */
    boolean canBeDeclared(EntityPlayer declarer);
}
