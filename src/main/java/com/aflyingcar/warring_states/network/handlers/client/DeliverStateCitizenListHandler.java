package com.aflyingcar.warring_states.network.handlers.client;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.network.messages.DeliverStateCitizenListMessage;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.TrackedMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.function.Consumer;

public class DeliverStateCitizenListHandler implements IMessageHandler<DeliverStateCitizenListMessage, IMessage> {
    @Override
    public IMessage onMessage(DeliverStateCitizenListMessage message, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            Consumer<TrackedMessage> consumer = NetworkUtils.popConsumerForResponse(message.getUUID());
            if(consumer == null) {
                WarringStatesMod.getLogger().warn("Received message with UUID " + message.getUUID() + " however no Consumer was found for that message.");
                return;
            }

            consumer.accept(message);
        });


        return null;
    }
}
