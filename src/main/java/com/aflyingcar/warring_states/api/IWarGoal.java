package com.aflyingcar.warring_states.api;

import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.util.INetSerializable;
import com.aflyingcar.warring_states.util.ISerializable;
import com.aflyingcar.warring_states.war.Conflict;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Base class for a war goal
 */
@ParametersAreNonnullByDefault
public interface IWarGoal extends ISerializable, INetSerializable {
    /**
     * Called when the owner of this wargoal joins a conflict for the firsts time
     * @param war The Conflict this wargoal is a part of
     * @param owner The owner of this wargoal
     */
    void onWarStarted(Conflict war, State owner);

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
     * Called when a player involved in the conflict is killed
     *
     * @param player The player killed
     * @param side   The side of the conflict the player was on
     */
    default void onPlayerDeath(EntityPlayer player, Conflict.Side side) { }

    /**
     * Called when a player involved in the conflict logs in
     *
     * @param player The player who logged out
     * @param side   The side of the conflict the player is on
     */
    default void onPlayerLogin(EntityPlayer player, Conflict.Side side) { }

    /**
     * Called when a player involved in the conflict logs out
     *
     * @param player The player who logged out
     * @param side   The side of the conflict the player is on
     */
    default void onPlayerLogout(EntityPlayer player, Conflict.Side side) { }

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

    /**
     * Creates a wargoal which the opposing side must have
     * @return A new wargoal that the opposing side gets to counter this one.
     */
    @Nonnull
    IWarGoal createOpposingWargoal();
}
