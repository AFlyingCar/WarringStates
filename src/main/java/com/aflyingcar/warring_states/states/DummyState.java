package com.aflyingcar.warring_states.states;

import com.aflyingcar.warring_states.util.ChunkGroup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DummyState extends State {
    public DummyState(UUID id, String name, String desc) {
        super(id, name, desc);
    }

    public DummyState(State state) {
        super(state.getUUID(), state.getName(), state.getDesc());
    }

    @Override
    public void addCitizen(UUID citizen, int privileges) { }

    @Override
    public boolean hasCitizen(UUID uuid) {
        return false;
    }

    @Override
    public int getPrivileges(UUID uuid) {
        return 0;
    }

    @Override
    public void setPrivileges(UUID uuid, int privileges) { }

    @Override
    public void apply(UUID uuid) { }

    @Override
    public NBTTagCompound writeNBT(NBTTagCompound nbt) {
        return null;
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
    }

    @Override
    public boolean hasPrivilege(UUID persistentID, int privilege) {
        return false;
    }

    @Nullable
    @Override
    public ChunkGroup getNearestChunkGroup(ChunkPos pos) {
        return null;
    }

    @Override
    public void claimTerritory(ChunkPos pos, int dimension) {
    }

    @Override
    public void unclaimTerritory(ChunkPos pos, int dimension) {
    }

    @Override
    public void updateAllClaimers() {
    }

    @Override
    public boolean hasApplicationFor(UUID persistentID) {
        return false;
    }

    @Override
    public List<ChunkPos> getClaimedTerritory() {
        return null;
    }

    @Override
    public Set<UUID> getCitizens() {
        return null;
    }
}
