package com.aflyingcar.warring_states.states;

import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.util.ChunkGroup;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.war.WarManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DummyState extends State {
    private boolean isAtWar;

    public DummyState(UUID id, String name, String desc) {
        super(id, name, desc);
    }

    public DummyState(State state) {
        super(state.getUUID(), state.getName(), state.getDesc());

        // Copy all of the other data
        setCapitalIndex(state.getCapitalIndex());
        setDecayTimer(state.getDecayTimer());
        setFormationTimer(state.getFormationTimer());
        setHasDecayed(state.hasDecayed());
        getControlledTerritory().addAll(state.getControlledTerritory());
        getAllApplications().addAll(state.getAllApplications());
        getCitizensWithPrivileges().putAll(state.getCitizensWithPrivileges());
    }

    public static DummyState readStateData(ByteBuf buf) {
        DummyState state = new DummyState(NetworkUtils.readUUID(buf), NetworkUtils.readString(buf), NetworkUtils.readString(buf));
        state.readData(buf);

        return state;
    }

    public static void writeStateData(State state, ByteBuf buf) {
        NetworkUtils.writeUUID(buf, state.getUUID());
        NetworkUtils.writeString(buf, state.getName());
        NetworkUtils.writeString(buf, state.getDesc());

        buf.writeInt(state.getCapitalIndex());

        NetworkUtils.writeTimer(buf, state.getDecayTimer());
        NetworkUtils.writeTimer(buf, state.getFormationTimer());

        buf.writeBoolean(state.hasDecayed());

        NetworkUtils.writeCollection(buf, state.getControlledTerritory(), NetworkUtils::writeChunkGroup);

        buf.writeBoolean(WarManager.getInstance().isAtWar(state));

        NetworkUtils.writeCollection(buf, state.getAllApplications(), NetworkUtils::writeUUID);
        NetworkUtils.writeMap(buf, state.getCitizensWithPrivileges(), NetworkUtils::writeUUID, ByteBuf::writeInt);
    }

    public void readData(ByteBuf buf) {
        setCapitalIndex(buf.readInt());
        setDecayTimer(NetworkUtils.readTimer(buf));
        setFormationTimer(NetworkUtils.readTimer(buf));
        setHasDecayed(buf.readBoolean());

        getControlledTerritory().clear();
        getControlledTerritory().addAll(NetworkUtils.readList(buf, NetworkUtils::readChunkGroup));

        isAtWar = buf.readBoolean();

        getAllApplications().addAll(NetworkUtils.readSet(buf, NetworkUtils::readUUID));
        getCitizensWithPrivileges().putAll(NetworkUtils.readMap(buf, NetworkUtils::readUUID, ByteBuf::readInt));
    }

    public void writeData(ByteBuf buf) {
        writeStateData(this, buf);
    }

    @Override
    public void addCitizen(UUID citizen, int privileges) { }

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
    public boolean unclaimTerritory(ChunkPos pos, int dimension) {
        return true;
    }

    @Override
    public void updateAllClaimers() {
    }

    @Override
    public List<ChunkPos> getClaimedTerritory() {
        return null;
    }

    @Override
    public void startDecayTimer() { }

    @Override
    public void stopDecayTimer() { }

    @Override
    public void declareWargoal(@Nonnull UUID target, @Nonnull IWarGoal goal) { }

    @Override
    public void onStateFounded() { }

    @Override
    public void onWarDeclaredOn(@Nonnull Set<UUID> targets) { }

    @Override
    public void onWarDeclaredBy(@Nonnull Set<UUID> belligerents) { }

    @Override
    public void onWarWon(@Nonnull Set<UUID> losers) { }

    @Override
    public void onWarLost(@Nonnull Set<UUID> winners) { }

    @Override
    public void revokeCitizenshipFor(UUID playerID) { }

    @Override
    public boolean acceptApplicationFor(UUID playerID) {
        return true;
    }

    @Override
    public void rejectApplicationFor(UUID playerID) { }

    @Override
    public void decayFurthestClaim() { }

    @Override
    public void setHasDecayed(boolean b) { }

    @Override
    public void kickPlayer(UUID citizenID) { }

    @Override
    public void sanityCheckAllClaimedTerritory(boolean fixProblems) { }

    public boolean isAtWar() {
        return isAtWar;
    }
}
