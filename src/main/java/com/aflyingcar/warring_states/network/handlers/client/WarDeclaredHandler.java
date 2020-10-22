package com.aflyingcar.warring_states.network.handlers.client;

import com.aflyingcar.warring_states.events.WarDeclaredEvent;
import com.aflyingcar.warring_states.network.messages.WarDeclaredMessage;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.war.DummyConflict;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class WarDeclaredHandler implements IMessageHandler<WarDeclaredMessage, IMessage> {
    @Override
    public IMessage onMessage(WarDeclaredMessage warDeclaredMessage, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            DummyConflict war = warDeclaredMessage.getWar();

            MinecraftForge.EVENT_BUS.post(new WarDeclaredEvent.CLIENT(war));
        });

        return null;
    }
}
