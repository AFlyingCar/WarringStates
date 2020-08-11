package com.aflyingcar.warring_states.war;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.events.WarCompleteEvent;
import com.aflyingcar.warring_states.events.WarDeclaredEvent;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.BaseManager;
import com.aflyingcar.warring_states.util.ExtendedBlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A manager for wars and conflicts
 */
public final class WarManager extends BaseManager {
    public static final String DATA_NAME = WarringStatesMod.MOD_ID + "_WARDATA";

    private static WarManager instance;

    private final List<Conflict> conflicts;

    private final List<Predicate<ExtendedBlockPos>> ignoreBlockBreakPredicate;
    private final List<Predicate<ExtendedBlockPos>> ignoreBlockPlacePredicate;

    private WarManager() {
        conflicts = new ArrayList<>();
        ignoreBlockBreakPredicate = new ArrayList<>();
        ignoreBlockPlacePredicate = new ArrayList<>();
    }

    public static WarManager getInstance() {
        if(instance == null)
            instance = new WarManager();

        return instance;
    }

    public void registerIgnoreBlockBreakPredicate(Predicate<ExtendedBlockPos> ignorePredicate) {
        ignoreBlockBreakPredicate.add(ignorePredicate);
    }

    public void registerIgnoreBlockPlacePredicate(Predicate<ExtendedBlockPos> ignorePredicate) {
        ignoreBlockPlacePredicate.add(ignorePredicate);
    }

    public boolean shouldBlockBreakBeIgnored(ExtendedBlockPos pos) {
        return ignoreBlockBreakPredicate.parallelStream().anyMatch(p -> p.test(pos));
    }

    public boolean shouldBlockPlaceBeIgnored(ExtendedBlockPos pos) {
        return ignoreBlockPlacePredicate.parallelStream().anyMatch(p -> p.test(pos));
    }

    @Override
    protected void readFromNBT(NBTTagCompound compound) {
        if(compound.hasKey("conflicts")) {
            for(NBTBase base : compound.getTagList("conflicts", 10)) {
                NBTTagCompound nbtConflict = (NBTTagCompound)base;

                Conflict conflict = new Conflict(new HashMap<>(), new HashMap<>());
                conflict.readNBT(nbtConflict);
                conflicts.add(conflict);
            }
        }
    }

    @Override
    protected NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList nbtConflicts = new NBTTagList();
        for(Conflict conflict : conflicts) {
            NBTTagCompound nbtConflict = new NBTTagCompound();

            conflict.writeNBT(nbtConflict);

            nbtConflicts.appendTag(nbtConflict);
        }
        compound.setTag("conflicts", nbtConflicts);

        return compound;
    }

    @Override
    protected File getDataFile(File rootGameDir) {
        return new File(rootGameDir, DATA_NAME + ".dat");
    }

    @Override
    public void resetAllData() {
        conflicts.clear();
    }

    private void registerConflict(Conflict conflict) {
        conflicts.add(conflict);
        markDirty();
    }

    /**
     * Checks whether this player is in a state that is currently at war
     * @param player The player to check
     * @return True if this player is in a state that is currently at war, false otherwise
     */
    public boolean isAtWar(EntityPlayer player) {
        State state = StateManager.getInstance().getStateFromPlayer(player);
        if(state == null)
            return false;

        for(Conflict c : conflicts) {
            if(c.getSideOf(state) != null) {
                return true;
            }
        }

        return false;
    }

    public boolean isAtWarWith(@Nonnull EntityPlayer player, State state) {
        return isAtWarWith(player.getPersistentID(), state);
    }

    /**
     * Checks if the given player UUID is in a state that is at war with {@code state}
     * @param uuid The player's UUID
     * @param state The state to check
     * @return true if there is currently a conflict between {@code uuid}'s state and state, false otherwise
     */
    public boolean isAtWarWith(@Nonnull UUID uuid, @Nonnull State state) {
        // Player is not at war with a state if they themselves are not a part of a state
        State state1 = StateManager.getInstance().getStateFromPlayerUUID(uuid);
        if(state1 == null) return false;

        return isAtWarWith(state1, state);
    }

    /**
     * Checks if two states are at war
     * @param state1 The first state to check
     * @param state2 The second state to check
     * @return true if there is currently a conflict between state1 and state2, false otherwise
     */
    public boolean isAtWarWith(State state1, State state2) {
        return getConflictBetween(state1, state2) != null;
    }

    @Override
    public void update() {
        // Check every conflict for a winner and remove it if the conflict has ended
        conflicts.removeIf(c -> (c.getWinner() != Conflict.Side.WAR_NOT_OVER) && endWar(c));
    }

    /**
     * What to do when a war ends
     * @param c The war that has just ended
     * @return True if the conflict should be removed from the list of conflicts, false otherwise
     */
    private boolean endWar(Conflict c) {
        WarCompleteEvent event = new WarCompleteEvent(c);
        MinecraftForge.EVENT_BUS.post(event);

        // If the WarCompleteEvent was not cancelled, go ahead and remove it from our tracking
        if(!event.isCanceled()) {
            markDirty();
            return true;
        }
        return false;
    }

    /**
     * Starts a war between two states
     * @param belligerent The attacker
     * @param goals The war goals of the attacker
     * @param target The state who is being declared war upon
     */
    public void startWar(@Nonnull State belligerent, @Nonnull NonNullList<IWarGoal> goals, State target) {
        Conflict conflict = new Conflict(target, belligerent, goals);

        // Allow war declaration to be cancelled
        if(!MinecraftForge.EVENT_BUS.post(new WarDeclaredEvent(conflict))) {
            registerConflict(conflict);
            markDirty();
        }
    }

    @Nullable
    public Conflict getConflictForID(int id) {
        return (id >= 0 && id < conflicts.size()) ? conflicts.get(id) : null;
    }

    /**
     * Gets the current conflict between the player and the given state, or null if there is no conflict between the two
     * @param player The player
     * @param state2 The state
     * @return A {@code Pair} of the conflict index and the conflict itself, or null if there is no conflict
     */
    @Nullable
    public Pair<Integer, Conflict> getConflictBetween(EntityPlayer player, State state2) {
        return getConflictBetween(StateManager.getInstance().getStateFromPlayer(player), state2);
    }

    /**
     * Gets the current conflict between the two given states, or null if there is no conflict between the two
     * @param state1 The first state
     * @param state2 The second state
     * @return A {@code Pair} of the conflict index and the conflict itself, or null if there is no conflict
     */
    @Nullable
    public Pair<Integer, Conflict> getConflictBetween(State state1, State state2) {
        // A state cannot be in conflict with a null state, so either of these being null instantly invalidates any
        //  possible conflict
        if(state1 == null || state2 == null) return null;

        int i = 0;
        for(Conflict conflict : conflicts) {
            ++i; // 1 too high

            // Skip this conflict if either state is not a part of it
            Conflict.Side side1 = conflict.getSideOf(state1);
            Conflict.Side side2 = conflict.getSideOf(state2);

            // state is at war with the other state if they are on opposing sides of the conflict
            if((side1 == null || side2 == null) || (side1 == side2)) continue;

            return Pair.of(i - 1, conflict);
        }

        return null;
    }

    /**
     * Gets all conflicts that involve the given State
     * @param state The state to check
     * @return A list of all conflicts that involve the given state
     */
    @Nonnull
    public List<Conflict> getAllConflictsInvolving(State state) {
        return conflicts.stream().filter(conflict -> conflict.getSideOf(state) != null).collect(Collectors.toList());
    }

    public void forceStopConflict(int conflictID) {
        conflicts.remove(conflictID);
        markDirty();
    }

    public List<Conflict> getAllConflicts() {
        return conflicts;
    }

    @Override
    public boolean isDirty() {
        // If we have any current conflicts, then this is always dirty as each conflict has a Timer that needs to be
        //  written to the disk
        return !conflicts.isEmpty() || super.isDirty();
    }
}
