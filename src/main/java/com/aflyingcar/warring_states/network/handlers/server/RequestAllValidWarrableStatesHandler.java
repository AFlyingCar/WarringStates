package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.network.messages.DeliverAllValidWarrableStatesMessage;
import com.aflyingcar.warring_states.network.messages.RequestAllValidWarrableStatesMessage;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.PlayerUtils;
import com.aflyingcar.warring_states.util.WarrableState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Objects;
import java.util.stream.Collectors;

public class RequestAllValidWarrableStatesHandler implements IMessageHandler<RequestAllValidWarrableStatesMessage, DeliverAllValidWarrableStatesMessage> {
    @Override
    public DeliverAllValidWarrableStatesMessage onMessage(RequestAllValidWarrableStatesMessage message, MessageContext ctx) {
        State state = StateManager.getInstance().getStateFromUUID(message.getStateID());

        if(state == null) {
            WarringStatesMod.getLogger().error("No such state found for UUID " + message.getStateID());
            return null;
        }

        if(!state.hasPrivilege(message.getPlayerID(), CitizenPrivileges.DECLARE_WAR)) {
            EntityPlayer player = PlayerUtils.getPlayerByUUID(message.getPlayerID());

            if(player != null)
                player.sendMessage(new TextComponentTranslation("warring_states.messages.declare_war.permission_denied", state.getName()));

            WarringStatesMod.getLogger().warn("Asked to get all states that can be declared war upon by " + message.getStateID() + ", but the player who requested it (" + message.getPlayerID() + ") doesn't have sufficient privileges to do so.");
            return null;
        }

        WarringStatesMod.getLogger().info("Delivering all valid warrable states.");

        return (DeliverAllValidWarrableStatesMessage) new DeliverAllValidWarrableStatesMessage(state.getWargoals().entrySet().stream().map(e -> {
            State s = StateManager.getInstance().getStateFromUUID(e.getKey());
            if(s == null) return null;

            return new WarrableState(s.getName(), e.getKey(), s.getCitizens().stream().filter(PlayerUtils::isPlayerOnline).collect(Collectors.toList()), e.getValue());
        }).filter(Objects::nonNull).collect(Collectors.toList())).setUUID(message.getUUID());

        /*
        // A state can only be declared war upon _if_ there is at least one player online at the time the war is declared
        return (DeliverAllValidWarrableStatesMessage) new DeliverAllValidWarrableStatesMessage(state.getWargoals().entrySet().stream().filter(e -> {
            State s = StateManager.getInstance().getStateFromUUID(e.getKey());
            return s != null && s.getCitizens().stream().anyMatch(PlayerUtils::isPlayerOnline);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))).setUUID(message.getUUID());
         */
    }
}
