package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.network.messages.DeliverConflictListMessage;
import com.aflyingcar.warring_states.network.messages.RequestConflictListMessage;
import com.aflyingcar.warring_states.war.WarManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RequestConflictListHandler implements IMessageHandler<RequestConflictListMessage, DeliverConflictListMessage> {
    @Override
    public DeliverConflictListMessage onMessage(RequestConflictListMessage message, MessageContext ctx) {
        return (DeliverConflictListMessage) new DeliverConflictListMessage(WarManager.getInstance().getAllConflicts()).setUUID(message.getUUID());
    }
}
