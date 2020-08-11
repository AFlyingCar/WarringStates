package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.network.messages.MoveCapitalToMessage;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.PlayerUtils;
import com.aflyingcar.warring_states.util.WorldUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Objects;

public class MoveCapitalToHandler implements IMessageHandler<MoveCapitalToMessage, IMessage> {
    @Override
    public IMessage onMessage(MoveCapitalToMessage message, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            State state = StateManager.getInstance().getStateFromUUID(message.getStateID());
            if(state == null) {
                WarringStatesMod.getLogger().error("Asked to move the capital of a state with UUID " + message.getStateID() + ", but could not find a state with that UUID.");
                return;
            }

            EntityPlayer player = PlayerUtils.getPlayerByUUID(message.getPlayerID());

            if(!state.hasPrivilege(message.getPlayerID(), CitizenPrivileges.MANAGEMENT)) {
                if(player != null) {
                    player.sendMessage(new TextComponentTranslation("warring_states.messages.move_capital.permission_denied", state.getName()));
                }
                WarringStatesMod.getLogger().warn("Asked to move the capital of " + state.getName() + ", but the player who asked " + message.getPlayerID() + " does not have sufficient permissions to do so.");
                return;
            }

            if(!state.setCapital(WorldUtils.getChunkFor(message.getPos()).getPos(), message.getPos().getDimID())) {
                if(player != null)
                    player.sendMessage(new TextComponentTranslation("warring_states.messages.move_capital.failure", state.getName()));
                WarringStatesMod.getLogger().error("Failed to set the capital for state " + state.getName());
            } else {
                state.getCitizens().stream().map(PlayerUtils::getPlayerByUUID).filter(Objects::nonNull).forEach(playerMP -> playerMP.sendMessage(new TextComponentTranslation("warring_states.messages.move_capital.success", state.getName(), message.getPos().toString())));
            }
        });

        return null;
    }
}
