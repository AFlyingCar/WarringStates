package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.util.NetworkUtils;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public class CitizenApplicationResultMessage implements IMessage {
    public enum Result {
        ACCEPT,
        REJECT
    }

    private UUID stateID;
    private UUID playerID;
    private UUID applierPlayerID;
    private Result result;

    public CitizenApplicationResultMessage() {
    }

    public CitizenApplicationResultMessage(UUID stateID, UUID playerID, UUID applierPlayerID, Result result) {
        this.stateID = stateID;
        this.playerID = playerID;
        this.applierPlayerID = applierPlayerID;
        this.result = result;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkUtils.writeUUID(buf, stateID);
        NetworkUtils.writeUUID(buf, playerID);
        NetworkUtils.writeUUID(buf, applierPlayerID);
        buf.writeInt(result.ordinal());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        stateID = NetworkUtils.readUUID(buf);
        playerID = NetworkUtils.readUUID(buf);
        applierPlayerID = NetworkUtils.readUUID(buf);
        result = Result.values()[buf.readInt()];
    }

    public UUID getStateID() {
        return stateID;
    }

    public UUID getPlayerID() {
        return playerID;
    }

    public Result getResult() {
        return result;
    }

    public UUID getApplierPlayerID() {
        return applierPlayerID;
    }
}
