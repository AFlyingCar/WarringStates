package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.network.messages.UpdatePlayerPrivilegesMessage;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.PlayerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdatePlayerPrivilegesHandler implements IMessageHandler<UpdatePlayerPrivilegesMessage, IMessage> {
    @Override
    public IMessage onMessage(UpdatePlayerPrivilegesMessage message, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            State state = StateManager.getInstance().getStateFromUUID(message.getStateID());

            if(state == null) {
                WarringStatesMod.getLogger().error("Asked to update player privileges for state with UUID " + message.getStateID() + ", but no such state was found!");
                return;
            }

            if(!state.hasPrivilege(message.getPlayerID(), CitizenPrivileges.MANAGEMENT)) {
                EntityPlayer player = PlayerUtils.getPlayerByUUID(message.getPlayerID());
                if(player != null) {
                    player.sendMessage(new TextComponentTranslation("warring_states.messages.update_citizens.permission_denied", state.getName()));
                }
                WarringStatesMod.getLogger().warn("Player UUID " + message.getPlayerID() + " asked to update the privileges of another citizen, but they do not have permission to do so themselves.");

                return;
            }

            state.setPrivileges(message.getCitizen().getCitizenID(), message.getCitizen().getPrivileges());
        });

        return null;
    }
}
