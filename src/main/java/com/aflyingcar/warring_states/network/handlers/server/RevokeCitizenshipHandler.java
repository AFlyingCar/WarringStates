package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.network.messages.RevokeCitizenshipMessage;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.NetworkUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class RevokeCitizenshipHandler implements IMessageHandler<RevokeCitizenshipMessage, IMessage> {
    @Override
    public IMessage onMessage(RevokeCitizenshipMessage message, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            UUID stateID = message.getStateID();
            UUID playerID = message.getPlayerID();

            State state = StateManager.getInstance().getStateFromUUID(stateID);

            if(state == null) {
                WarringStatesMod.getLogger().warn("No such state for UUID " + stateID + "! Something wrong is happening!");
                return;
            }

            if(state.hasCitizen(playerID)) {
                state.revokeCitizenshipFor(playerID);
            } else {
                WarringStatesMod.getLogger().warn("Asked to revoke citizenship for player " + playerID + ", but they are not currently a citizen?");
            }
        });

        return null;
    }
}
