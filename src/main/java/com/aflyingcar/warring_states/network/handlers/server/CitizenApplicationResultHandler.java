package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.api.WarringStatesAPI;
import com.aflyingcar.warring_states.network.messages.CitizenApplicationResultMessage;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.PlayerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class CitizenApplicationResultHandler implements IMessageHandler<CitizenApplicationResultMessage, IMessage> {
    @Override
    public IMessage onMessage(CitizenApplicationResultMessage message, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            UUID stateID = message.getStateID();
            UUID playerID = message.getPlayerID();
            UUID applierPlayerID = message.getApplierPlayerID();
            CitizenApplicationResultMessage.Result result = message.getResult();

            State state = StateManager.getInstance().getStateFromUUID(stateID);

            if(state == null) {
                WarringStatesMod.getLogger().error("Asked to handle the result of a player applying for a state with UUID " + stateID + ", but no such state for that ID exists!");
                return;
            }

            if(!state.hasPrivilege(playerID, CitizenPrivileges.RECRUITMENT)) {
                EntityPlayer player = PlayerUtils.getPlayerByUUID(playerID);
                if(player != null)
                    player.sendMessage(new TextComponentTranslation("warring_states.messages.recruitment.permission_denied", state.getName()));

                WarringStatesMod.getLogger().warn("Player of UUID " + playerID + " asked us to handle the result of a player application for state " + state.getName() + ", but they do not have sufficient permissions.");
                return;
            }

            switch(result) {
                case ACCEPT:
                    WarringStatesAPI.acceptCitizenshipApplication(state, playerID, applierPlayerID);
                    break;
                case REJECT:
                    WarringStatesAPI.rejectCitizenshipApplication(state, playerID, applierPlayerID);
                    break;
            }
        });

        return null;
    }
}
