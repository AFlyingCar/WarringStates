package com.aflyingcar.warring_states.war;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import com.aflyingcar.warring_states.util.Timer;
import com.aflyingcar.warring_states.util.*;
import com.aflyingcar.warring_states.war.goals.StealChunkWarGoal;
import com.aflyingcar.warring_states.war.goals.WarGoalFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a conflict between two sides
 */
public class Conflict implements ISerializable {
    private final Map<State, NonNullList<IWarGoal>> defenders;
    private final Map<State, NonNullList<IWarGoal>> belligerents;

    private final Set<BlockPos> capturedClaimers;

    private final Timer warStartTimer;
    private Side forcedWinner = null;

    public Conflict(@Nonnull Map<State, NonNullList<IWarGoal>> defenders, @Nonnull Map<State, NonNullList<IWarGoal>> belligerents) {
        this.defenders = defenders;
        this.belligerents = belligerents;
        this.warStartTimer = new Timer();

        this.capturedClaimers = Sets.newHashSet();
    }

    public Conflict(@Nonnull State defender, @Nonnull State belligerent, @Nonnull NonNullList<IWarGoal> belligerentGoals) {
        this.defenders = new HashMap<>();
        this.belligerents = new HashMap<>();
        this.capturedClaimers = Sets.newHashSet();
        this.warStartTimer = new Timer();

        this.defenders.put(defender, NonNullList.create());
        belligerentGoals.forEach(g -> defenders.get(defender).add(g.createOpposingWargoal()));

        this.belligerents.put(belligerent, belligerentGoals);
    }

    @Override
    public NBTTagCompound writeNBT(NBTTagCompound nbt) {
        NBTTagList nbtDefenders = new NBTTagList();
        NBTTagList nbtBelligerents = new NBTTagList();

        for(Map.Entry<State, NonNullList<IWarGoal>> d : defenders.entrySet()) {
            NBTTagCompound nbtDefender = new NBTTagCompound();

            nbtDefender.setString("name", d.getKey().getName());
            nbtDefender.setTag("goals", NBTUtils.serializeCollection(d.getValue()));

            nbtDefenders.appendTag(nbtDefender);
        }

        for(Map.Entry<State, NonNullList<IWarGoal>> b : belligerents.entrySet()) {
            NBTTagCompound nbtBelligerent = new NBTTagCompound();

            nbtBelligerent.setString("name", b.getKey().getName());
            nbtBelligerent.setTag("goals", NBTUtils.serializeCollection(b.getValue()));

            nbtBelligerents.appendTag(nbtBelligerent);
        }

        nbt.setTag("defenders", nbtDefenders);
        nbt.setTag("belligerents", nbtBelligerents);

        NBTTagCompound timerTag = new NBTTagCompound();
        nbt.setTag("warStartTimer", warStartTimer.writeNBT(timerTag));

        // nbt.setFloat("timeSinceWarStarted", timeSinceWarStarted);

        return nbt;
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        // Make sure we have all tags, otherwise this file may be broken
        if(!(nbt.hasKey("defenders") && nbt.hasKey("belligerents"))) {
            WarringStatesMod.getLogger().error("Conflict NBT data is missing one or more of the following tags: 'defenders' or 'belligerents'. Verify this file before it gets overwritten. We will not be loading this conflict's data.");
            // TODO: Throw here so that whoever constructed us can know to throw us in the garbage where we belong
            return;
        }

        // Read in all of the defenders in this war
        NBTTagList nbtDefenders = nbt.getTagList("defenders", 10);
        for(NBTBase base : nbtDefenders) {
            NBTTagCompound defender = (NBTTagCompound)base;

            State state = StateManager.getInstance().getStateFromName(defender.getString("name"));
            NonNullList<IWarGoal> goals = NBTUtils.deserializeList(defender.getTagList("goals", 10), WarGoalFactory::newWargoal);

            defenders.put(state, goals);
        }

        // Read in all of the belligerents in this war
        NBTTagList nbtBelligerents = nbt.getTagList("belligerents", 10);
        for(NBTBase base : nbtBelligerents) {
            NBTTagCompound belligerent = (NBTTagCompound)base;

            State state = StateManager.getInstance().getStateFromName(belligerent.getString("name"));
            NonNullList<IWarGoal> goals = NBTUtils.deserializeList(belligerent.getTagList("goals", 10), WarGoalFactory::newWargoal);

            belligerents.put(state, goals);

            // belligerents.add(StateManager.getInstance().getStateFromName(((NBTTagString)base).getString()));
        }

        // timeSinceWarStarted = nbt.getFloat("timeSinceWarStarted");
        if(nbt.hasKey("warStartTimer")) {
            NBTTagCompound timerTag = nbt.getCompoundTag("warStartTimer");
            warStartTimer.readNBT(timerTag);
        }
    }

    /**
     * Begins the global war timer for this Conflict
     */
    public void startWarTimer() {
        warStartTimer.start();
    }

    @Deprecated
    public void saveChangeForRollback(RestorableBlock restorableBlock) {
        WarManager.getInstance().saveChangeForRollback(restorableBlock, this);
    }

    @Deprecated
    public void rollbackChanges() {
        WarManager.getInstance().rollbackChangesFor(this);
    }

    @Deprecated
    public List<RestorableBlock> getRestorableBlocks() {
        return WarManager.getInstance().getRestorableBlocksFor(this);
    }

    @Nonnull
    public List<IWarGoal> getWargoalsForSide(Side side) {
        if(side == null)
            return Lists.newArrayList();

        switch(side) {
            case DEFENDER:
                return defenders.values().parallelStream().flatMap(Collection::parallelStream).collect(Collectors.toList());
            case BELLIGERENT:
                return belligerents.values().parallelStream().flatMap(Collection::parallelStream).collect(Collectors.toList());
            default:
                return Lists.newArrayList();
        }
    }

    /**
     * Forces the winner of this conflict
     * Generally this should not be used, and conflicts should rely solely on whether all of the wargoals for a given side have been accomplished
     * Note as well that forcedWinner is never serialized, and will not persist between server restarts.
     * @param side The side to force as the winner
     */
    public void forceSetWinner(Side side) {
        forcedWinner = side;
    }

    public List<IWarGoal> getWargoals() {
        return Stream.concat(defenders.values().parallelStream().flatMap(Collection::parallelStream),
                             belligerents.values().parallelStream().flatMap(Collection::parallelStream)).collect(Collectors.toList());
    }

    /**
     * An enumeration for determining the side of a conflict (usually in terms of winners versus losers)
     */
    public enum Side {
        DEFENDER,
        BELLIGERENT,
        WAR_NOT_OVER,
    }

    @Nonnull
    public Map<State, NonNullList<IWarGoal>> getDefenders() {
        return defenders;
    }

    @Nonnull
    public Map<State, NonNullList<IWarGoal>> getBelligerents() {
        return belligerents;
    }

    public Timer getWarStartTimer() {
        return warStartTimer;
    }

    /**
     * Captures a {@code TileEntityClaimer} and accomplishes all war goals that expect it to be captured
     * @param claimer The {@code TileEntityClaimer} to capture
     */
    public void captureClaimer(TileEntityClaimer claimer) {
        capturedClaimers.add(claimer.getPos());

        // Go over every goal for every belligerent state
        for(IWarGoal goal : belligerents.values().stream().flatMap(Collection::stream).collect(Collectors.toList())) {
            if(goal instanceof StealChunkWarGoal && WorldUtils.isBlockWithinChunk(((StealChunkWarGoal)goal).getChunk(), claimer.getPos())) {
                ((StealChunkWarGoal)goal).accomplish();
            }
        }

        // Go over every goal for every defender state
        for(IWarGoal goal : defenders.values().stream().flatMap(Collection::stream).collect(Collectors.toList())) {
            if(goal instanceof StealChunkWarGoal && WorldUtils.isBlockWithinChunk(((StealChunkWarGoal)goal).getChunk(), claimer.getPos())) {
                ((StealChunkWarGoal)goal).accomplish();
            }
        }
    }

    @Nonnull
    public Set<BlockPos> getCapturedClaimers() {
        return capturedClaimers;
    }

    /**
     * Allows a {@code State} to join this {@code Conflict}
     * @param state The state that wants to join
     * @param side The side that the {@code state} wants to join
     * @param goals The war goals of the {@code state}
     */
    public void joinWar(@Nonnull State state, @Nonnull Side side, @Nullable NonNullList<IWarGoal> goals) {
        switch(side) {
            case DEFENDER:
                if(defenders.containsKey(state)) {
                    // TODO: Send message and disallow joining the war (no duplicates)
                    return;
                }
                defenders.put(state, goals == null ? NonNullList.create() : goals);
                break;
            case BELLIGERENT:
                if(belligerents.containsKey(state)) {
                    // TODO: Send message and disallow joining the war (no duplicates)
                    return;
                }
                belligerents.put(state, goals == null ? NonNullList.create() : goals);
                break;
            default:
                WarringStatesMod.getLogger().error("Invalid war side '" + side + "'. Must be either DEFENDER or BELLIGERENT.");
        }
    }

    /**
     * Gets the winner of this {@code Conflict}.
     * A winner is determined by whether or not all of their goals for this war have been met, with the belligerents getting priority
     * @return The winner of this {@code Conflict}, or {@code WAR_NOT_OVER} if the conflict is still ongoing
     */
    @Nonnull
    public Side getWinner() {
        if(forcedWinner != null) return forcedWinner;

        if(belligerents.values().stream().flatMap(Collection::stream).allMatch((goal) -> goal.accomplished(this))) {
             return Side.BELLIGERENT;
        } else if(defenders.values().stream().flatMap(Collection::stream).allMatch((goal) -> goal.accomplished(this))) {
            return Side.DEFENDER;
        } else {
            return Side.WAR_NOT_OVER;
        }
    }

    /**
     * Gets the side that this state is on
     * @param state The state to get the side of
     * @return The side that {@code state} is on, or {@code null} if they are not involved in this war.
     */
    @Nullable
    public Side getSideOf(@Nonnull State state) {
        if(defenders.containsKey(state)) {
            return Side.DEFENDER;
        } else if(belligerents.containsKey(state)) {
            return Side.BELLIGERENT;
        } else {
            // TODO: We should actually probably throw an exception instead
            return null;
        }
    }

    @Override
    public String toString() {
        return "D=[" + defenders.entrySet().stream().map(e -> '{' + e.getKey().getName() + ',' + e.getValue().size() + "},").collect(Collectors.joining()) +
               "], B=[" + belligerents.entrySet().stream().map(e -> '{' + e.getKey().getName() + ',' + e.getValue().size() + "},").collect(Collectors.joining()) + "]";
    }
}
