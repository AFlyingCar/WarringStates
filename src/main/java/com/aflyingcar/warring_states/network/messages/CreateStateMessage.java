package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.util.ExtendedBlockPos;
import com.aflyingcar.warring_states.util.NetworkUtils;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public class CreateStateMessage implements IMessage {
    private ExtendedBlockPos capital;
    private String name;
    private String desc;
    private UUID stateID;
    private UUID founder;
    private boolean canceled;

    public CreateStateMessage() {
    }

    public CreateStateMessage(ExtendedBlockPos position, String name, String desc, UUID stateID, UUID founder) {
        this.capital = position;
        this.name = name;
        this.desc = desc;
        this.stateID = stateID;
        this.founder = founder;
        this.canceled = false;
    }

    public CreateStateMessage(ExtendedBlockPos position) {
        this.capital = position;
        this.canceled = true;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        capital = NetworkUtils.readExtendedBlockPos(buf);
        canceled = buf.readBoolean();

        if(!canceled) {
            name = NetworkUtils.readString(buf);
            desc = NetworkUtils.readString(buf);
            stateID = NetworkUtils.readUUID(buf);
            founder = NetworkUtils.readUUID(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkUtils.writeExtendedBlockPos(buf, capital);
        buf.writeBoolean(canceled);

        if(!canceled) {
            NetworkUtils.writeString(buf, name);
            NetworkUtils.writeString(buf, desc);
            NetworkUtils.writeUUID(buf, stateID);
            NetworkUtils.writeUUID(buf, founder);
        }
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public ExtendedBlockPos getCapital() {
        return capital;
    }

    public UUID getStateID() {
        return stateID;
    }

    public UUID getFounder() {
        return founder;
    }

    public boolean isCanceled() {
        return canceled;
    }
}
