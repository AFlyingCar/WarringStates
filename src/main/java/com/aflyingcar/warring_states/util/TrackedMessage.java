package com.aflyingcar.warring_states.util;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public abstract class TrackedMessage implements IMessage {
    private UUID uuid = UUID.randomUUID();

    public TrackedMessage() { }

    @Override
    public void fromBytes(ByteBuf buf) {
        uuid = NetworkUtils.readUUID(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkUtils.writeUUID(buf, uuid);
    }

    public UUID getUUID() {
        return uuid;
    }

    public TrackedMessage setUUID(UUID uuid) {
        this.uuid = uuid;
        return this;
    }
}
