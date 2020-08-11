package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.network.messages.KickPlayerFromStateMessage;
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

public class KickPlayerFromStateHandler implements IMessageHandler<KickPlayerFromStateMessage, IMessage> {
    @Override
    public IMessage onMessage(KickPlayerFromStateMessage message, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            State state = StateManager.getInstance().getStateFromUUID(message.getStateID());

            if(state == null) {
                WarringStatesMod.getLogger().error("Asked to kick a player for state with UUID " + message.getStateID() + ", but no such state was found!");
                return;
            }

            if(!state.hasPrivilege(message.getPlayerID(), CitizenPrivileges.MANAGEMENT)) {
                EntityPlayer player = PlayerUtils.getPlayerByUUID(message.getPlayerID());
                if(player != null) {
                    player.sendMessage(new TextComponentTranslation("warring_states.messages.kick_citizen.permission_denied", state.getName()));
                }
                WarringStatesMod.getLogger().warn("Player UUID " + message.getPlayerID() + " asked to kick another citizen, but they do not have permission to do so.");

                return;
            }

            state.kickPlayer(message.getCitizen().getCitizenID());

            EntityPlayer kickedCitizen = PlayerUtils.getPlayerByUUID(message.getCitizen().getCitizenID());
            if(kickedCitizen != null) {
                kickedCitizen.sendMessage(new TextComponentTranslation("warring_states.messages.you_have_been_kicked", state.getName(), PlayerUtils.getPlayerNameFromUUID(message.getPlayerID())));
            }

            state.getCitizens().stream().map(PlayerUtils::getPlayerByUUID).filter(Objects::nonNull).forEach(player -> player.sendMessage(new TextComponentTranslation("warring_states.messages.player_has_been_kicked_by", message.getCitizen().getName(), state.getName(), PlayerUtils.getPlayerNameFromUUID(message.getPlayerID()))));
        });

        return null;
    }
}
