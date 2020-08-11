package com.aflyingcar.warring_states.network.handlers.client;

import com.aflyingcar.warring_states.network.messages.DeliverStateCitizenApplicationListMessage;
import com.aflyingcar.warring_states.util.NetworkUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DeliverStateCitizenApplicationListHandler implements IMessageHandler<DeliverStateCitizenApplicationListMessage, IMessage> {
    @Override
    public IMessage onMessage(DeliverStateCitizenApplicationListMessage message, MessageContext ctx) {
        NetworkUtils.receivedTrackedResponse(message, ctx);

        return null;
    }
}
