package com.aflyingcar.warring_states.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class OpenGuiMessage implements IMessage {
    private int guiID;
    private BlockPos pos;
    private int privileges;

    public OpenGuiMessage() { }

    public OpenGuiMessage(int guiID, BlockPos pos, int privileges) {
        this.guiID = guiID;
        this.pos = pos;
        this.privileges = privileges;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        guiID = buf.readInt();
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        privileges = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(guiID);
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(privileges);
    }

    public int getGuiID() {
        return guiID;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getPrivileges() {
        return privileges;
    }
}
