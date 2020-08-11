package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.network.messages.DeliverStateCitizenApplicationListMessage;
import com.aflyingcar.warring_states.network.messages.RequestStateCitizenApplicationListMessage;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.PlayerUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class RequestStateCitizenApplicationListHandler implements IMessageHandler<RequestStateCitizenApplicationListMessage, DeliverStateCitizenApplicationListMessage> {
    @Override
    public DeliverStateCitizenApplicationListMessage onMessage(RequestStateCitizenApplicationListMessage message, MessageContext ctx) {
        State state = StateManager.getInstance().getStateFromUUID(message.getStateID());

        if(state == null) {
            WarringStatesMod.getLogger().error("No such state for requested UUID " + message.getStateID());
            return null;
        }

        Map<UUID, String> uuidNameMap = state.getAllApplications().stream().filter(e -> Objects.nonNull(PlayerUtils.getPlayerNameFromUUID(e))).collect(Collectors.toMap(e -> e, e -> Objects.requireNonNull(PlayerUtils.getPlayerNameFromUUID(e))));

        return (DeliverStateCitizenApplicationListMessage) new DeliverStateCitizenApplicationListMessage(uuidNameMap).setUUID(message.getUUID());
    }
}
