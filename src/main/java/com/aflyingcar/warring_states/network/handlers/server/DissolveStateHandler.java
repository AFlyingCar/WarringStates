package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.api.WarringStatesAPI;
import com.aflyingcar.warring_states.network.messages.DissolveStateMessage;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.PlayerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Objects;

public class DissolveStateHandler implements IMessageHandler<DissolveStateMessage, IMessage> {
    @Override
    public IMessage onMessage(DissolveStateMessage message, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            State state = StateManager.getInstance().getStateFromUUID(message.getStateID());

            if(state == null) {
                WarringStatesMod.getLogger().error("Asked to dissolve state with UUID " + message.getStateID() + ", but no such state exists with that UUID.");
                return;
            }

            if(!state.hasPrivilege(message.getPlayerID(), CitizenPrivileges.ALL)) {
                EntityPlayer player = PlayerUtils.getPlayerByUUID(message.getPlayerID());

                if(player != null)
                    player.sendMessage(new TextComponentTranslation("warring_states.messages.dissolve_state.permission_denied", state.getName()));

                WarringStatesMod.getLogger().warn("Asked to dissolve " + message.getStateID() + ", but the player who requested it (" + message.getPlayerID() + ") doesn't have sufficient privileges to do so.");
                return;
            }

            WarringStatesAPI.dissolveState(state, Objects.requireNonNull(PlayerUtils.getPlayerByUUID(message.getPlayerID())));
        });

        return null;
    }
}
