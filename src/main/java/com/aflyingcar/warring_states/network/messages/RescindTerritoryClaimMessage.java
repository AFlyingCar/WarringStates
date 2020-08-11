package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.util.ExtendedBlockPos;
import com.aflyingcar.warring_states.util.NetworkUtils;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public class RescindTerritoryClaimMessage implements IMessage {
    private UUID stateID;
    private ExtendedBlockPos pos;
    private UUID requestingPlayerID;

    public RescindTerritoryClaimMessage() {
    }

    public RescindTerritoryClaimMessage(UUID stateID, ExtendedBlockPos pos, UUID playerUUID) {
        this.stateID = stateID;
        this.pos = pos;
        this.requestingPlayerID = playerUUID;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        stateID = NetworkUtils.readUUID(buf);
        pos = NetworkUtils.readExtendedBlockPos(buf);
        requestingPlayerID = NetworkUtils.readUUID(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkUtils.writeUUID(buf, stateID);
        NetworkUtils.writeExtendedBlockPos(buf, pos);
        NetworkUtils.writeUUID(buf, requestingPlayerID);
    }

    public UUID getStateID() {
        return stateID;
    }

    public ExtendedBlockPos getPos() {
        return pos;
    }

    public UUID getRequestingPlayerID() {
        return requestingPlayerID;
    }
}
