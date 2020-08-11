package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.network.messages.DeliverStateCitizenListMessage;
import com.aflyingcar.warring_states.network.messages.RequestStateCitizenListMessage;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RequestStateCitizenListHandler implements IMessageHandler<RequestStateCitizenListMessage, DeliverStateCitizenListMessage> {
    @Override
    public DeliverStateCitizenListMessage onMessage(RequestStateCitizenListMessage message, MessageContext ctx) {
        State state = StateManager.getInstance().getStateFromUUID(message.getStateID());

        if(state == null) {
            WarringStatesMod.getLogger().error("No such state for ID " + message.getStateID());
            return null;
        }

        return (DeliverStateCitizenListMessage) new DeliverStateCitizenListMessage(state.getCitizensWithPrivileges()).setUUID(message.getUUID());
    }
}
