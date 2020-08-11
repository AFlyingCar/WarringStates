package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.util.NetworkUtils;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class UpdateStateInfoMessage implements IMessage {
    /**
     * The format that the incoming data will take. Valid identifiers are as follows:
     *  N == Name (string)
     *  D == Desc (string)
     */
    private String dataFormat;

    private UUID requestingPlayerID;
    private UUID stateID;
    private String newName;
    private String desc;

    public UpdateStateInfoMessage() { }

    public UpdateStateInfoMessage(@Nonnull UUID requestingPlayerID, @Nonnull UUID stateID, @Nonnull String newName, String desc) {
        this.dataFormat = "ND";

        this.requestingPlayerID = requestingPlayerID;
        this.stateID = stateID;
        this.newName = newName;
        this.desc = desc;
    }

    public UpdateStateInfoMessage(@Nonnull UUID requestingPlayerID, @Nonnull UUID stateID) {
        this.dataFormat = "";
        this.requestingPlayerID = requestingPlayerID;
        this.stateID = stateID;
    }

    public UpdateStateInfoMessage(@Nonnull UUID requestingPlayerID, @Nonnull UUID stateID, @Nonnull String newName) {
        this.dataFormat = "N";
        this.requestingPlayerID = requestingPlayerID;
        this.stateID = stateID;
        this.newName = newName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        requestingPlayerID = NetworkUtils.readUUID(buf);
        stateID = NetworkUtils.readUUID(buf);
        dataFormat = NetworkUtils.readString(buf);

        for(char c : dataFormat.toCharArray()) {
            switch(c) {
                case 'N':
                    newName = NetworkUtils.readString(buf);
                    break;
                case 'D':
                    desc = NetworkUtils.readString(buf);
                    break;
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkUtils.writeUUID(buf, requestingPlayerID);
        NetworkUtils.writeUUID(buf, stateID);
        NetworkUtils.writeString(buf, dataFormat);

        for(char c : dataFormat.toCharArray()) {
            switch(c) {
                case 'N':
                    NetworkUtils.writeString(buf, newName);
                    break;
                case 'D':
                    NetworkUtils.writeString(buf, desc);
                    break;
            }
        }
    }

    @Nonnull
    public UUID getStateID() {
        return stateID;
    }

    @Nullable
    public String getNewName() { return newName; }

    @Nullable
    public String getDesc() {
        return desc;
    }

    @Nonnull
    public UUID getRequestingPlayerID() {
        return requestingPlayerID;
    }
}
