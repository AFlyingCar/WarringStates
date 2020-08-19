package com.aflyingcar.warring_states.states;

import com.aflyingcar.warring_states.WarringStatesConfig;
import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import com.aflyingcar.warring_states.util.Timer;
import com.aflyingcar.warring_states.util.*;
import com.aflyingcar.warring_states.war.goals.WarGoalFactory;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a single State
 */
public class State implements ISerializable {
    // Maximum grouping of 16 chunks (TODO: This should be a config option)
    // TODO: Should this be here, or in ChunkGroup?
    private static final int MAX_CHUNKGROUP_SIZE = 8;

    private String name;
    private String desc;

    private final Map<UUID, Integer> citizens;
    private final Set<UUID> citizenApplications;

    private final List<ChunkGroup> controlledTerritory;

    private int capital = -1;

    private Map<UUID, Set<IWarGoal>> wargoals;

    private UUID uuid; // TODO: This needs to get used by the rest of the mod instead of the name

    private final Timer decayTimer;
    private boolean hasDecayed;

    public State() {
        this(null, "", "");
    }

    public State(String name, String desc, UUID uuid, UUID founder) {
        this(uuid, name, desc);

        addCitizen(founder, CitizenPrivileges.ALL);
    }

    protected State(UUID id, String name, String desc) {
        this.uuid = id;
        this.name = name;
        this.desc = desc;

        this.citizens = new HashMap<>();
        this.wargoals = new HashMap<>();
        this.citizenApplications = new HashSet<>();
        this.controlledTerritory = new ArrayList<>();
        this.decayTimer = new Timer();
    }

    public void startDecayTimer() {
        decayTimer.start();
    }

    public void stopDecayTimer() {
        decayTimer.stop();
    }

    public void addCitizen(UUID citizen, int privileges) {
        citizens.put(citizen, privileges);

        WarringStatesMod.proxy.markStateManagerDirty();
    }

    public boolean hasCitizen(UUID uuid) {
        return citizens.containsKey(uuid);
    }

    public int getPrivileges(UUID uuid) {
        return citizens.getOrDefault(uuid, 0);
    }

    public void setPrivileges(UUID uuid, int privileges) {
        if(citizens.containsKey(uuid)) {
            citizens.put(uuid, privileges);

            WarringStatesMod.proxy.markStateManagerDirty();
        }
    }

    public void apply(UUID uuid) {
        citizenApplications.add(uuid);

        WarringStatesMod.proxy.markStateManagerDirty();
    }

    public final Set<UUID> getAllApplications() {
        return citizenApplications;
    }

    public void declareWargoal(@Nonnull UUID target, @Nonnull IWarGoal goal) {
        wargoals.putIfAbsent(target, new HashSet<>());
        wargoals.get(target).add(goal);
        WarringStatesMod.proxy.markStateManagerDirty();
    }

    @Nullable
    public Set<IWarGoal> getWargoalsAgainst(@Nonnull UUID target) {
        return wargoals.getOrDefault(target, null);
    }

    /**
     * Called when this state declares war on another
     * @param targets The defenders of this State's aggression
     */
    public void onWarDeclaredOn(@Nonnull Set<UUID> targets) {
        // War has started, so remove all of our wargoals for the given targets (we will assume they have been already moved into the correct Conflict)
        for(UUID target : targets) wargoals.remove(target);
        WarringStatesMod.proxy.markStateManagerDirty();
    }

    /**
     * Called when this state is declared war upon
     * @param belligerents The aggressors being defended against
     */
    public void onWarDeclaredBy(@Nonnull Set<UUID> belligerents) {
        // TODO
    }

    /**
     * Called when this state wins a war
     * @param losers The losers of the conflict
     */
    public void onWarWon(@Nonnull Set<UUID> losers) {
        // TODO
    }

    /**
     * Called when this state loses a war
     * @param winners The winners of the conflict
     */
    public void onWarLost(@Nonnull Set<UUID> winners) {
        // TODO
    }

    @Override
    public NBTTagCompound writeNBT(NBTTagCompound nbt) {
        nbt.setString("name", name);
        nbt.setString("desc", desc);
        nbt.setUniqueId("uuid", uuid);

        NBTTagList nbtCitizenList = new NBTTagList();
        for(Map.Entry<UUID, Integer> citizen : citizens.entrySet()) {
            // Note: This is a compound tag because we may store additional information here in the future
            NBTTagCompound nbtCitizen = new NBTTagCompound();
            nbtCitizen.setUniqueId("uuid", citizen.getKey());
            nbtCitizen.setInteger("privileges", citizen.getValue());

            nbtCitizenList.appendTag(nbtCitizen);
        }
        nbt.setTag("citizens", nbtCitizenList);

        NBTTagList nbtApplicationsList = new NBTTagList();
        for(UUID application : citizenApplications) {
            nbtApplicationsList.appendTag(NBTUtil.createUUIDTag(application));
        }
        nbt.setTag("applications", nbtApplicationsList);

        NBTTagList nbtChunkGroups = new NBTTagList();
        for(ChunkGroup chunkGroup : controlledTerritory) {
            NBTTagCompound nbtChunkGroup = new NBTTagCompound();
            chunkGroup.writeNBT(nbtChunkGroup);
            nbtChunkGroups.appendTag(nbtChunkGroup);
        }
        nbt.setTag("controlledTerritory", nbtChunkGroups);

        nbt.setTag("wargoals", NBTUtils.serializeMap(wargoals, entry -> {
            NBTTagCompound nbtEntry = new NBTTagCompound();
            nbtEntry.setUniqueId("key", entry.getKey());
            nbtEntry.setTag("val", NBTUtils.serializeCollection(entry.getValue()));
            return nbtEntry;
        }));

        // TODO: Should capital be stored as an index? Or should we store the physical ChunkGroup instead?
        nbt.setInteger("capital", capital);

        nbt.setTag("decayTimer", decayTimer.writeNBT(new NBTTagCompound()));
        nbt.setBoolean("hasDecayed", hasDecayed);

        return nbt;
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        name = nbt.getString("name");
        desc = nbt.getString("desc");
        uuid = nbt.getUniqueId("uuid");

        NBTTagList nbtCitizenList = nbt.getTagList("citizens", 10); // COMPOUND
        for(NBTBase citizen : nbtCitizenList) {
            UUID uuid = ((NBTTagCompound)citizen).getUniqueId("uuid");
            int privileges = ((NBTTagCompound)citizen).getInteger("privileges");
            addCitizen(uuid, privileges);
        }

        if(nbt.hasKey("applications")) {
            NBTTagList nbtApplicationsList = nbt.getTagList("applications", 10); // COMPOUND
            for(NBTBase application : nbtApplicationsList) {
                UUID uuid = NBTUtil.getUUIDFromTag((NBTTagCompound) application);
                apply(uuid);
            }
        }

        if(nbt.hasKey("controlledTerritory")) {
            NBTTagList nbtChunkGroups = nbt.getTagList("controlledTerritory", 10); // COMPOUND
            for(NBTBase nbtBase : nbtChunkGroups) {
                ChunkGroup group = new ChunkGroup();
                group.readNBT((NBTTagCompound) nbtBase);
                controlledTerritory.add(group);
            }
        }

        if(nbt.hasKey("wargoals")) {
            NBTTagList nbtWargoals = nbt.getTagList("wargoals", 10); // COMPOUND
            wargoals = NBTUtils.deserializeMap(nbtWargoals, nbtBase -> {
                UUID uuid = ((NBTTagCompound) nbtBase).getUniqueId("key");
                NonNullList<IWarGoal> wargoals = NBTUtils.deserializeList(((NBTTagCompound) nbtBase).getTagList("val", 10), WarGoalFactory::newWargoal);

                return Pair.of(uuid, new HashSet<>(wargoals));
            });
        }

        if(nbt.hasKey("capital")) {
            capital = nbt.getInteger("capital");
        }

        if(nbt.hasKey("decayTimer")) {
            decayTimer.readNBT(nbt.getCompoundTag("decayTimer"));
        }

        if(nbt.hasKey("hasDecayed")) {
            hasDecayed = nbt.getBoolean("hasDecayed");
        }
    }

    public String getName() {
        return name;
    }

    public boolean hasPrivilege(UUID persistentID, int privilege) {
        return (getPrivileges(persistentID) & privilege) == privilege;
    }

    /**
     * Find the nearest {@code ChunkGroup} that this {@code ChunkPos} can be a part of
     * @param pos The position of a chunk to check
     * @return The nearest {@code ChunkGroup} for this {@code ChunkPos}, or null if none work.
     */
    @Nullable
    public ChunkGroup getNearestChunkGroup(ChunkPos pos) {
        return getNearestChunkGroup(pos, null);
    }

    /**
     * Finds the nearest {@code ChunkGroup} that this {@code ChunkPos} can be a part of
     * @param pos The position of a chunk to check
     * @param blacklist A blacklist of {@code ChunkGroups} that {@code pos} cannot be a part of
     * @return The nearest {@code ChunkGroup} for this {@code ChunkPos} that is not in {@code blacklist}, or null if none work.
     */
    @Nullable
    private ChunkGroup getNearestChunkGroup(ChunkPos pos, @SuppressWarnings("SameParameterValue") @Nullable List<ChunkGroup> blacklist) {
        for(ChunkGroup group : controlledTerritory) {
            if((blacklist == null || !blacklist.contains(group)) && group.isChunkNearby(pos)) {
                return group;
            }
        }

        return null;
    }

    /**
     * Finds the nearest {@code ChunkGroup} to the given {@code ChunkPos} that is not full
     * @param pos The position to search with
     * @return The nearest {@code ChunkGroup} to the given {@code ChunkPos} that is not full, or null if none are found
     */
    @Nullable
    private ChunkGroup getNearestNonFullChunkGroup(ChunkPos pos) {
        for(ChunkGroup group : controlledTerritory) {
            if(group.getChunks().size() < MAX_CHUNKGROUP_SIZE && group.isChunkNearby(pos))
                return group;
        }

        return null;
    }

    /**
     * Finds the first {@code ChunkGroup} which contains the given {@code ChunkPos}.
     * @param pos The {@code ChunkPos} to find.
     * @param dimension The dimension ID that the given {@code ChunkPos} refers into
     * @return The first {@code ChunkGroup} that contains the given {@code ChunkPos}.
     */
    @Nullable
    private ChunkGroup getContainingChunkGroup(@Nonnull ChunkPos pos, int dimension) {
        for(ChunkGroup group : controlledTerritory) {
            // Note: Do the dimension check first because it's faster, allowing us to potentially exit early
            if(group.getDimension() == dimension && group.getChunks().contains(pos)) {
                return group;
            }
        }

        return null;
    }

    /**
     * Claims a chunk of territory for this {@code State}
     * @param pos The chunk position to claim
     */
    public void claimTerritory(ChunkPos pos, int dimension) {
        // Find a chunk group to place this chunk position in
        ChunkGroup group = getNearestNonFullChunkGroup(pos);

        // Create a new one if we couldn't find an existing ChunkGroup
        if(group == null) {
            group = new ChunkGroup(dimension);
            controlledTerritory.add(group);
        }

        // Add the position to the ChunkGroup
        group.addChunk(pos);

        // If we do not have a capital, then set it to the very next claimed chunk
        if(getCapital() == null) {
            capital = controlledTerritory.indexOf(group);
        }

        WarringStatesMod.proxy.markStateManagerDirty();
    }

    public void unclaimTerritory(ChunkPos pos, int dimension) {
        ChunkGroup group = getContainingChunkGroup(pos, dimension);

        if(WarringStatesConfig.preventUnclaimingCapital) {
            int groupIndex = controlledTerritory.indexOf(group);
            if(groupIndex == capital) {
                return;
            }
        }

        if(group != null) {
            group.removeChunk(pos);

            if(group.isEmpty()) {
                // Make sure to update the index of our capital, since controlledTerritory changes size and things may shift
                ChunkGroup capital = getCapital();

                controlledTerritory.remove(group);

                this.capital = (capital == null ? -1 : controlledTerritory.indexOf(capital));
            }
        }

        WarringStatesMod.proxy.markStateManagerDirty();
    }

    public void setDescription(String desc) {
        this.desc = desc;
        WarringStatesMod.proxy.markStateManagerDirty();
    }

    /**
     * Two {@code States} are equivalent if they share a UUID
     * @param o The object to check
     * @return True if the UUIDs, types, or pointers are the same, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof State && ((State)o).getUUID().equals(uuid));
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public boolean hasApplicationFor(UUID persistentID) {
        return citizenApplications.contains(persistentID);
    }

    public void setName(String name) {
        this.name = name;
        WarringStatesMod.proxy.markStateManagerDirty();
    }

    public List<ChunkPos> getClaimedTerritory() {
        return controlledTerritory.stream().map(ChunkGroup::getChunks).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public Set<UUID> getCitizens() {
        return citizens.keySet();
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * Updates the state information of all claimers controlled by this state
     */
    public void updateAllClaimers() {
        for(ChunkGroup group : controlledTerritory) {
            int dimension = group.getDimension();
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension);
            for(ChunkPos chunk : group.getChunks()) {
                WorldUtils.getTileEntitiesWithinChunk(TileEntityClaimer.class, world, chunk).forEach(claimer -> claimer.changeOwner(name, desc, uuid));
            }
        }
    }

    public boolean doesControlTerritory(ChunkPos position, int dimension) {
        return getContainingChunkGroup(position, dimension) != null;
    }

    @Nullable
    public ChunkGroup getCapital() {
        // If the capital is not a valid index, then just return null
        return (capital < 0 || capital > controlledTerritory.size()) ? null : controlledTerritory.get(capital);
    }

    /**
     * Sets the capital of this State to point at the given ChunkPos in the given dimension.
     * {@code capital} will not be changed if {@code ChunkPos,dimension} is not owned by this State
     * @param position The position to point at.
     * @param dimension The dimension the {@code ChunkPos} is in.
     * @return true if {@code capital} was updated, false otherwise
     */
    public boolean setCapital(@Nonnull ChunkPos position, int dimension) {
        int capitalIndex = controlledTerritory.indexOf(getContainingChunkGroup(position, dimension));
        if(capitalIndex < 0) {
            WarringStatesMod.getLogger().warn("Cannot set capital to a chunk we do not own.");
            return false;
        } else {
            capital = capitalIndex;
            WarringStatesMod.proxy.markStateManagerDirty();
            return true;
        }
    }

    public void revokeCitizenshipFor(UUID playerID) {
        citizens.remove(playerID);
        WarringStatesMod.proxy.markStateManagerDirty();
    }

    public Map<UUID, Integer> getCitizensWithPrivileges() {
        return citizens;
    }

    public Set<UUID> getWargoalTargets() {
        return wargoals.keySet();
    }

    public Map<UUID, Set<IWarGoal>> getWargoals() {
        return wargoals;
    }

    public boolean acceptApplicationFor(UUID playerID) {
        if(citizenApplications.remove(playerID)) {
            addCitizen(playerID, WarringStatesConfig.defaultNewlyAppliedPrivileges);
            return true;
        } else {
            WarringStatesMod.getLogger().warn("Player ID " + playerID + " was not found in the set of citizen applications!");
            return false;
        }
    }

    public void rejectApplicationFor(UUID playerID) {
        citizenApplications.remove(playerID);
        WarringStatesMod.proxy.markStateManagerDirty();
    }

    public Timer getDecayTimer() {
        return decayTimer;
    }

    public void decayFurthestClaim() {
        // TODO: Find furthest claim from capital
        // TODO: unclaim() furthest
        setHasDecayed(true);
    }

    public void setHasDecayed(boolean b) {
        hasDecayed = b;
        WarringStatesMod.proxy.markStateManagerDirty();
    }

    public boolean hasDecayed() {
        return hasDecayed;
    }

    public void kickPlayer(UUID citizenID) {
        citizens.remove(citizenID);
        WarringStatesMod.proxy.markStateManagerDirty();
    }

    /**
     * Verifies that all territory claimed by this State contains a BlockClaimer+TileEntityClaimer _somewhere_ in the chunk
     * This is a potentially expensive operation, and should be done rarely
     */
    public void sanityCheckAllClaimedTerritory(boolean fixProblems) {
        WarringStatesMod.getLogger().info("Beginning sanity check of all territory controlled by " + this);
        int problems = 0;
        List<Pair<ChunkPos, Integer>> toUnclaim = new ArrayList<>();
        for(ChunkGroup claimed : controlledTerritory) {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(claimed.getDimension());
            for(ChunkPos chunkPos : claimed.getChunks()) {
                List<TileEntityClaimer> claimers = WorldUtils.getTileEntitiesWithinChunk(TileEntityClaimer.class, world, chunkPos);

                if(claimers.size() > 1) {
                    WarringStatesMod.getLogger().warn("Multiple TileEntityClaimers found in chunk " + chunkPos + ".");
                    if(fixProblems) {
                        WarringStatesMod.getLogger().warn("Destroying all but the first which belong to us!");
                        boolean found = false;
                        for(TileEntityClaimer claimer : claimers) {
                            if(claimer.getStateUUID() == null) {
                                //noinspection ResultOfMethodCallIgnored
                                WorldUtils.destroyClaimer(new ExtendedBlockPos(claimer.getPos(), claimed.getDimension()));
                            } else if(claimer.getStateUUID().equals(getUUID())) {
                                if(found) {
                                    //noinspection ResultOfMethodCallIgnored
                                    WorldUtils.destroyClaimer(new ExtendedBlockPos(claimer.getPos(), claimed.getDimension()));
                                } else {
                                    found = true;
                                }
                            }
                        }
                    }
                    ++problems;
                } else if(claimers.isEmpty()) {
                    WarringStatesMod.getLogger().warn("No claimer found in chunk " + chunkPos + " which we claim to control!");
                    if(fixProblems) {
                        WarringStatesMod.getLogger().warn("Relinquishing our claim on this chunk!");
                        toUnclaim.add(Pair.of(chunkPos, claimed.getDimension()));
                    }
                    ++problems;
                } else { // Only 1 claimer, but let's perform a sanity check on it as well
                    problems += claimers.get(0).performSanityCheck(fixProblems);
                }
            }
        }

        if(fixProblems) {
            for(Pair<ChunkPos, Integer> info : toUnclaim) {
                unclaimTerritory(info.getLeft(), info.getRight());
            }
        }

        WarringStatesMod.getLogger().info("Sanity check complete. " + problems + (fixProblems ? " problems fixed." : " problems detected."));
    }

    @Override
    public String toString() {
        return "State{UUID=" + uuid + ", Name=" + name + "}";
    }
}

