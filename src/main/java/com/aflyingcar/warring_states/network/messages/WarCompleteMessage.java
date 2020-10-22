package com.aflyingcar.warring_states.network.messages;

import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.war.Conflict;
import com.aflyingcar.warring_states.war.DummyConflict;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class WarCompleteMessage implements IMessage {
    private DummyConflict war;

    public WarCompleteMessage() {
    }

    public WarCompleteMessage(Conflict war) {
        this.war = new DummyConflict(war);
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        war = NetworkUtils.readConflict(byteBuf);
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        NetworkUtils.writeConflict(byteBuf, war);
    }

    public DummyConflict getWar() {
        return war;
    }
}
