package com.aflyingcar.warring_states.util;

import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.war.goals.WarGoalFactory;
import io.netty.buffer.ByteBuf;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Contains information on a state that can be declared war upon
 */
public class WarrableState implements INetSerializable {
    private String targetStateName;
    private UUID targetStateID;
    private List<UUID> onlinePlayers;
    private Collection<IWarGoal> warGoals;

    public WarrableState() {

    }

    public WarrableState(String targetStateName, UUID targetStateID, List<UUID> onlinePlayers, Collection<IWarGoal> warGoals) {
        this.targetStateName = targetStateName;
        this.targetStateID = targetStateID;
        this.onlinePlayers = onlinePlayers;
        this.warGoals = warGoals;
    }

    public String getTargetStateName() {
        return targetStateName;
    }

    public UUID getTargetStateID() {
        return targetStateID;
    }

    public Collection<IWarGoal> getWarGoals() {
        return warGoals;
    }

    public List<UUID> getOnlinePlayers() {
        return onlinePlayers;
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        NetworkUtils.writeString(buf, targetStateName);
        NetworkUtils.writeUUID(buf, targetStateID);
        NetworkUtils.writeCollection(buf, onlinePlayers, NetworkUtils::writeUUID);
        NetworkUtils.writeCollection(buf, warGoals, (b, t) -> {
            NetworkUtils.writeString(b, t.getClass().getName());
            NetworkUtils.writeNetSerializable(b, t);
        });
    }

    @Override
    public void readFromBuf(ByteBuf buf) {
        targetStateName = NetworkUtils.readString(buf);
        targetStateID = NetworkUtils.readUUID(buf);
        onlinePlayers = NetworkUtils.readList(buf, NetworkUtils::readUUID);
        warGoals = NetworkUtils.readList(buf, WarGoalFactory::newWargoal).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static WarrableState createFromBuf(ByteBuf buf) {
        WarrableState state = new WarrableState();

        state.readFromBuf(buf);

        return state;
    }
}

