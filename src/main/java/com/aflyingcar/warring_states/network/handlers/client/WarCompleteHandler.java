package com.aflyingcar.warring_states.network.handlers.client;

import com.aflyingcar.warring_states.events.WarCompleteEvent;
import com.aflyingcar.warring_states.network.messages.WarCompleteMessage;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.war.DummyConflict;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class WarCompleteHandler implements IMessageHandler<WarCompleteMessage, IMessage> {
    @Override
    public IMessage onMessage(WarCompleteMessage warCompleteMessage, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            DummyConflict war = warCompleteMessage.getWar();

            MinecraftForge.EVENT_BUS.post(new WarCompleteEvent.CLIENT(war));
        });

        return null;
    }
}
