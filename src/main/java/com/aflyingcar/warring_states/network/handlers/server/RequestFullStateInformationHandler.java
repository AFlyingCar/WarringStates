package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.network.messages.DeliverFullStateInformationMessage;
import com.aflyingcar.warring_states.network.messages.RequestFullStateInformationMessage;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RequestFullStateInformationHandler implements IMessageHandler<RequestFullStateInformationMessage, DeliverFullStateInformationMessage> {
    @Override
    public DeliverFullStateInformationMessage onMessage(RequestFullStateInformationMessage message, MessageContext ctx) {
        State state = StateManager.getInstance().getStateFromUUID(message.getStateID());

        if(state == null) {
            WarringStatesMod.getLogger().error("No such state found for ID " + message.getStateID());
            return null;
        }

        return (DeliverFullStateInformationMessage)(new DeliverFullStateInformationMessage(state).setUUID(message.getUUID()));
    }
}
