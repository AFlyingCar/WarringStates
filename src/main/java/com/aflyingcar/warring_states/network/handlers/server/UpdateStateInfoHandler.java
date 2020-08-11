package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.network.messages.UpdateStateInfoMessage;
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

public class UpdateStateInfoHandler implements IMessageHandler<UpdateStateInfoMessage, IMessage> {
    @Override
    public IMessage onMessage(UpdateStateInfoMessage message, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            UUID requestingPlayerID = message.getRequestingPlayerID();
            UUID stateID = message.getStateID();
            String newName = message.getNewName();
            String desc = message.getDesc();

            State state = StateManager.getInstance().getStateFromUUID(stateID);

            if(state != null) {
                // Make sure nobody is being naughty
                if(!state.hasPrivilege(requestingPlayerID, CitizenPrivileges.MANAGEMENT)) {
                    EntityPlayer player = PlayerUtils.getPlayerByUUID(requestingPlayerID);
                    if(player != null)
                        player.sendMessage(new TextComponentTranslation("warring_states.messages.management.permission_denied", state.getName()));
                    WarringStatesMod.getLogger().warn("Got a manage state data request from player UUID " + requestingPlayerID + ", but they do not have such permissions! This should never have been able to happen legit.");
                    return;
                }

                boolean updated_values = false;

                if(newName != null) {
                    state.setName(newName);
                    updated_values = true;
                }

                if(desc != null) {
                    state.setDescription(desc);
                    updated_values = true;
                }

                if(updated_values) {
                    state.updateAllClaimers();
                }
            }
        });

        return null;
    }
}
