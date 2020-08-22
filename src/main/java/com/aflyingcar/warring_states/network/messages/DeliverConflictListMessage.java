package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.states.DummyState;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.TrackedMessage;
import com.aflyingcar.warring_states.war.Conflict;
import com.aflyingcar.warring_states.war.DummyConflict;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.stream.Collectors;

public class DeliverConflictListMessage extends TrackedMessage {
    private List<DummyConflict> conflicts;

    public DeliverConflictListMessage() {
    }

    public DeliverConflictListMessage(List<Conflict> conflicts) {
        this.conflicts = conflicts.stream().map(DummyConflict::new).collect(Collectors.toList());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        conflicts = NetworkUtils.readList(buf,
                byteBuf -> new DummyConflict(NetworkUtils.readMap(byteBuf, DummyState::readStateData, ByteBuf::readInt),
                                             NetworkUtils.readMap(byteBuf, DummyState::readStateData, ByteBuf::readInt)));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        NetworkUtils.writeCollection(buf, conflicts, (byteBuf, conflict) -> {
            NetworkUtils.writeMap(buf, conflict.getBelligerents(), (byteBuf2, state) -> DummyState.writeStateData(state, byteBuf2), ByteBuf::writeInt);
            NetworkUtils.writeMap(buf, conflict.getDefenders(), (byteBuf2, state) -> DummyState.writeStateData(state, byteBuf2), ByteBuf::writeInt);
        });
    }

    public List<DummyConflict> getConflicts() {
        return conflicts;
    }
}
