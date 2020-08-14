package com.aflyingcar.warring_states.war;

import com.aflyingcar.warring_states.WarringStatesConfig;
import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.events.WarCompleteEvent;
import com.aflyingcar.warring_states.events.WarDeclaredEvent;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.Timer;
import com.aflyingcar.warring_states.util.*;
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

    private Map<ExtendedBlockPos, Pair<RestorableBlock, Integer>> restorableBlockConflictMapping;
    private Map<Integer, Set<ExtendedBlockPos>> conflictModificationMapping;

    private Map<UUID, Timer> warWaitTimers;

    private WarManager() {
        conflicts = new ArrayList<>();
        ignoreBlockBreakPredicate = new ArrayList<>();
        ignoreBlockPlacePredicate = new ArrayList<>();

        restorableBlockConflictMapping = new HashMap<>();
        conflictModificationMapping = new HashMap<>();
        warWaitTimers = new HashMap<>();
    }

    public static WarManager getInstance() {
        if(instance == null)
            instance = new WarManager();

        return instance;
    }

    /**
     * Clears all rollback operations and marks the WarManager as dirty
     * Meant for debugging purposes only! Not recommended to use
     */
    public void clearRollbackOperations() {
        restorableBlockConflictMapping.clear();
        conflictModificationMapping.clear();
        markDirty();
    }

    /**
     * Saves a rollback operation for a change that has occurred in the world.
     * @param block
     * @param conflict
     */
    public void saveChangeForRollback(RestorableBlock block, Conflict conflict) {
        ExtendedBlockPos blockPos = block.getPos();
        int cindex = conflicts.indexOf(conflict);

        // Do we already have a restoration operation at this position?
        if(restorableBlockConflictMapping.containsKey(blockPos)) {
            Pair<RestorableBlock, Integer> conflictPairing = restorableBlockConflictMapping.get(blockPos);

            // First check if the type of restoration operation needs to change
            if(block.isReplacementOperation()) {
                conflictPairing.getLeft().setPlacedBlock(block.getPlacedBlock());
            } else {
                conflictPairing.getLeft().setPlacedBlock(null);
            }

            // Update count if this conflict is modifying this block for the first time
            if(!conflictModificationMapping.containsKey(cindex) || !conflictModificationMapping.get(cindex).contains(blockPos)) {
                conflictModificationMapping.putIfAbsent(cindex, new HashSet<>());
                conflictModificationMapping.get(cindex).add(blockPos);
                int numConflicts = conflictPairing.getValue();
                restorableBlockConflictMapping.put(blockPos, Pair.of(conflictPairing.getLeft(), numConflicts + 1));
            }

            markDirty();
            return;
        } else {
            restorableBlockConflictMapping.put(blockPos, Pair.of(block, 0));
        }
        RestorableBlock restorableBlock = restorableBlockConflictMapping.get(blockPos).getKey();
        int numConflicts = restorableBlockConflictMapping.get(blockPos).getValue();
        restorableBlockConflictMapping.put(blockPos, Pair.of(restorableBlock, numConflicts + 1));

        conflictModificationMapping.putIfAbsent(cindex, new HashSet<>());
        conflictModificationMapping.get(cindex).add(blockPos);

        markDirty();
    }

    public List<RestorableBlock> getRestorableBlocksFor(Conflict conflict) {
        int cindex = conflicts.indexOf(conflict);

        Set<ExtendedBlockPos> positions = conflictModificationMapping.get(cindex);

        return Collections.unmodifiableList(positions.stream().filter(restorableBlockConflictMapping::containsKey).map(restorableBlockConflictMapping::get).map(Pair::getLeft).collect(Collectors.toList()));
    }

    public void rollbackChangesFor(Conflict conflict) {
        int cindex = conflicts.indexOf(conflict);
        if(cindex < 0) {
            WarringStatesMod.getLogger().error("Unable to find conflict in list of current conflicts! indexOf returned -1");
            return;
        }

        Set<ExtendedBlockPos> modifiedPositions = conflictModificationMapping.get(cindex);

        if(modifiedPositions != null) {
            for(ExtendedBlockPos pos : modifiedPositions) {
                Pair<RestorableBlock, Integer> restorablePair = restorableBlockConflictMapping.getOrDefault(pos, null);
                if(restorablePair == null) {
                    WarringStatesMod.getLogger().error("Have position cached for rollback, but no restorable block was found cached with this position! Skipping " + pos);
                } else {
                    // Decrease by 1
                    int newConflictCount = restorablePair.getRight() - 1;

                    // If no more conflicts are tracking this changed position, then restore it
                    if(newConflictCount <= 0) {
                        restorablePair.getLeft().restore();
                        restorableBlockConflictMapping.remove(pos);
                    } else {
                        restorableBlockConflictMapping.put(pos, Pair.of(restorablePair.getLeft(), newConflictCount));
                    }
                }
            }
        }

        conflictModificationMapping.remove(cindex); // Remove from the mapping
        markDirty();
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

        if(compound.hasKey("restorableBlockMapping")) {
            NBTTagList nbtEntries = compound.getTagList("restorableBlockMapping", 10);
            restorableBlockConflictMapping = NBTUtils.deserializeMap(nbtEntries, nbtBase -> {
                NBTTagCompound nbtEntry = ((NBTTagCompound)nbtBase);

                ExtendedBlockPos pos = new ExtendedBlockPos(nbtEntry.getInteger("x"), nbtEntry.getInteger("y"), nbtEntry.getInteger("z"), nbtEntry.getInteger("dimID"));
                Pair<RestorableBlock, Integer> pair = Pair.of(new RestorableBlock(), nbtEntry.getInteger("numConflicts"));
                pair.getLeft().readNBT(nbtEntry.getCompoundTag("restorable"));

                return Pair.of(pos, pair);
            });
        }

        if(compound.hasKey("conflictModificationMapping")) {
            NBTTagList nbtEntries = compound.getTagList("conflictModificationMapping", 10);
            conflictModificationMapping = NBTUtils.deserializeMap(nbtEntries, nbtBase -> {
                NBTTagCompound nbtEntry = ((NBTTagCompound)nbtBase);

                int conflictIdx = nbtEntry.getInteger("conflictIndex");
                NonNullList<ExtendedBlockPos> positions = NBTUtils.deserializeGenericList(nbtEntry.getTagList("positions", 10), base -> {
                    NBTTagCompound nbtPos = ((NBTTagCompound)base);
                    return new ExtendedBlockPos(nbtPos.getInteger("x"), nbtPos.getInteger("y"), nbtPos.getInteger("z"), nbtPos.getInteger("dimID"));
                });

                return Pair.of(conflictIdx, new HashSet<>(positions));
            });
        }

        if(compound.hasKey("warWaitTimers")) {
            warWaitTimers = NBTUtils.deserializeMap(compound.getTagList("warWaitTimers", 10), base -> {
                UUID stateID = ((NBTTagCompound)base).getUniqueId("stateID");
                NBTTagCompound nbtTimer = ((NBTTagCompound)base).getCompoundTag("timer");
                Timer timer = new Timer();
                timer.readNBT(nbtTimer);

                return Pair.of(stateID, timer);
            });
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

        compound.setTag("restorableBlockMapping", NBTUtils.serializeMap(restorableBlockConflictMapping, entry -> {
            NBTTagCompound nbtEntry = new NBTTagCompound();
            nbtEntry.setInteger("x", entry.getKey().getX());
            nbtEntry.setInteger("y", entry.getKey().getY());
            nbtEntry.setInteger("z", entry.getKey().getZ());
            nbtEntry.setInteger("dimID", entry.getKey().getDimID());

            nbtEntry.setTag("restorable", entry.getValue().getKey().writeNBT(new NBTTagCompound()));
            nbtEntry.setInteger("numConflicts", entry.getValue().getValue());

            return nbtEntry;
        }));

        compound.setTag("conflictModificationMapping", NBTUtils.serializeMap(conflictModificationMapping, entry -> {
            NBTTagCompound nbtEntry = new NBTTagCompound();

            nbtEntry.setInteger("conflictIndex", entry.getKey());
            nbtEntry.setTag("positions", NBTUtils.serializeCollection(entry.getValue(), pos -> {
                NBTTagCompound nbtPos = new NBTTagCompound();
                nbtPos.setInteger("x", pos.getX());
                nbtPos.setInteger("y", pos.getY());
                nbtPos.setInteger("z", pos.getZ());
                nbtPos.setInteger("dimID", pos.getDimID());
                return nbtPos;
            }));

            return nbtEntry;
        }));

        compound.setTag("warWaitTimers", NBTUtils.serializeMap(warWaitTimers, entry -> {
            NBTTagCompound nbtEntry = new NBTTagCompound();

            nbtEntry.setUniqueId("stateID", entry.getKey());
            nbtEntry.setTag("timer", entry.getValue().writeNBT(new NBTTagCompound()));

            return nbtEntry;
        }));

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

        return isAtWar(state);
    }

    /**
     * Checks whether this state is currently in a war
     * @param state The state to check
     * @return True if this state is currently in a war, false otherwise.
     */
    public boolean isAtWar(@Nonnull State state) {
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

    public boolean canParticipateInWar(State state) {
        Timer timer = warWaitTimers.getOrDefault(state.getUUID(), null);

        if(timer == null || timer.getNumberOfHours() > WarringStatesConfig.numHoursBetweenWarAttempts) {
            warWaitTimers.remove(state.getUUID());
            return true;
        } else {
            return false;
        }
    }

    public long getRemainingWarWaitTimerHoursFor(State state) {
        Timer timer = warWaitTimers.getOrDefault(state.getUUID(), null);
        return timer == null ? 0 : timer.getNumberOfHours();
    }

    public void startWarWaitTimerFor(State state) {
        Timer timer;
        warWaitTimers.put(state.getUUID(), timer = new Timer());
        timer.start();
    }

    public void startWarWaitTimers(Conflict war) {
        for(State state : war.getDefenders().keySet()) {
            startWarWaitTimerFor(state);
        }
        for(State state : war.getBelligerents().keySet()) {
            startWarWaitTimerFor(state);
        }
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
