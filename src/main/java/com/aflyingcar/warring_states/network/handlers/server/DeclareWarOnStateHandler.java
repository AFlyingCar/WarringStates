package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.api.WarringStatesAPI;
import com.aflyingcar.warring_states.network.messages.DeclareWarOnStateMessage;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.PlayerUtils;
import com.aflyingcar.warring_states.util.PredicateUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DeclareWarOnStateHandler implements IMessageHandler<DeclareWarOnStateMessage, IMessage> {
    @Override
    public IMessage onMessage(DeclareWarOnStateMessage message, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            State state = StateManager.getInstance().getStateFromUUID(message.getStateID());

            EntityPlayer player = PlayerUtils.getPlayerByUUID(message.getPlayerID());

            if(player == null) {
                WarringStatesMod.getLogger().error("War cannot be declared by a bad player UUID!");
                return;
            }

            if(state == null) {
                WarringStatesMod.getLogger().warn("No such state was found for UUID " + message.getStateID());
                return;
            }
            State playerState = StateManager.getInstance().getStateFromPlayerUUID(message.getPlayerID());
            if(playerState == null) {
                WarringStatesMod.getLogger().error("Player " + player.getName() + " is not a part of a known state! Cannot declare war.");
                return;
            }

            if(!playerState.hasPrivilege(message.getPlayerID(), CitizenPrivileges.DECLARE_WAR)) {
                WarringStatesMod.getLogger().warn("Asked to declare war on " + message.getStateID() + ", but the player who requested it (" + message.getPlayerID() + ") doesn't have sufficient privileges to do so.");
                return;
            }

            if(state.getCitizens().stream().allMatch(PredicateUtils.not(PlayerUtils::isPlayerOnline))) {
                player.sendMessage(new TextComponentTranslation("warring_states.messages.declare_war.nobody_online"));
                WarringStatesMod.getLogger().warn("Cannot declare war on a state when nobody is online to defend.");
                return;
            }

            WarringStatesAPI.declareWarOn(player, playerState, state);
        });

        return null;
    }
}
