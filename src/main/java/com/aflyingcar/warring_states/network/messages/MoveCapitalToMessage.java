package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.util.ExtendedBlockPos;
import com.aflyingcar.warring_states.util.NetworkUtils;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public class MoveCapitalToMessage implements IMessage {
    private UUID stateID;
    private UUID playerID;
    private ExtendedBlockPos pos;

    public MoveCapitalToMessage() {
    }

    public MoveCapitalToMessage(UUID stateID, UUID playerID, ExtendedBlockPos pos) {
        this.stateID = stateID;
        this.playerID = playerID;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        stateID = NetworkUtils.readUUID(buf);
        playerID = NetworkUtils.readUUID(buf);
        pos = NetworkUtils.readExtendedBlockPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkUtils.writeUUID(buf, stateID);
        NetworkUtils.writeUUID(buf, playerID);
        NetworkUtils.writeExtendedBlockPos(buf, pos);
    }

    public UUID getStateID() {
        return stateID;
    }

    public UUID getPlayerID() {
        return playerID;
    }

    public ExtendedBlockPos getPos() {
        return pos;
    }
}
