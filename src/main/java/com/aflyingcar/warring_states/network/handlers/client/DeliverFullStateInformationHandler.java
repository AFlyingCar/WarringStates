package com.aflyingcar.warring_states.network.handlers.client;

import com.aflyingcar.warring_states.network.messages.DeliverFullStateInformationMessage;
import com.aflyingcar.warring_states.util.NetworkUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DeliverFullStateInformationHandler implements IMessageHandler<DeliverFullStateInformationMessage, IMessage> {
    @Override
    public IMessage onMessage(DeliverFullStateInformationMessage message, MessageContext ctx) {
        NetworkUtils.receivedTrackedResponse(message, ctx);

        return null;
    }
}
