package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.events.CitizenApplicationEvent;
import com.aflyingcar.warring_states.network.messages.CitizenAppliesMessage;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.NetworkUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class CitizenAppliesHandler implements IMessageHandler<CitizenAppliesMessage, IMessage> {
    @Override
    public IMessage onMessage(CitizenAppliesMessage message, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            UUID newCitizen = message.getCitizenUUID();
            UUID stateID = message.getStateID();

            State state = StateManager.getInstance().getStateFromUUID(stateID);

            if(state != null) {
                // Apply for citizen-ship, but first lets make sure that nobody else wants to cancel that
                CitizenApplicationEvent event = new CitizenApplicationEvent(state, newCitizen);
                MinecraftForge.EVENT_BUS.post(event);

                if(!event.isCanceled()) {
                    state.apply(newCitizen);
                }
            } else {
                WarringStatesMod.getLogger().warn("Player " + newCitizen + " is attempting to apply to a state ID of " + stateID + " that does not exist!");
            }
        });

        return null;
    }
}
