package com.aflyingcar.warring_states.network.handlers.client;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.network.messages.DeliverAllValidWarrableStatesMessage;
import com.aflyingcar.warring_states.util.NetworkUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DeliverAllValidWarrableStatesHandler implements IMessageHandler<DeliverAllValidWarrableStatesMessage, IMessage> {
    @Override
    public IMessage onMessage(DeliverAllValidWarrableStatesMessage message, MessageContext ctx) {
        WarringStatesMod.getLogger().info("Received all valid warrable states.");
        NetworkUtils.receivedTrackedResponse(message, ctx);

        return null;
    }
}
