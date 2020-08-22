package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.network.messages.RescindTerritoryClaimMessage;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.ExtendedBlockPos;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.PlayerUtils;
import com.aflyingcar.warring_states.util.WorldUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class RescindTerritoryClaimHandler implements IMessageHandler<RescindTerritoryClaimMessage, IMessage> {
    @Override
    public IMessage onMessage(RescindTerritoryClaimMessage message, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            UUID stateID = message.getStateID();
            ExtendedBlockPos pos = message.getPos();
            UUID requestingPlayerID = message.getRequestingPlayerID();

            State state = StateManager.getInstance().getStateFromUUID(stateID);

            if(state == null) {
                WarringStatesMod.getLogger().warn("No such state for UUID " + stateID);
                return;
            }

            EntityPlayer player = PlayerUtils.getPlayerByUUID(requestingPlayerID);

            // Make sure nobody is being naughty
            if(!state.hasPrivilege(requestingPlayerID, CitizenPrivileges.CLAIM_TERRITORY)) {
                if(player != null)
                    player.sendMessage(new TextComponentTranslation("warring_states.messages.rescind_claim.permission_denied", state.getName()));
                WarringStatesMod.getLogger().warn("Got a rescind territory request from player UUID " + requestingPlayerID + ", but they do not have such permissions! This should never have been able to happen legit.");
                return;
            }

            World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(pos.getDimID());

            if(!state.unclaimTerritory(world.getChunk(pos).getPos(), pos.getDimID())) {
                if(player != null)
                    player.sendMessage(new TextComponentTranslation("warring_states.messages.cannot_rescind_capitol"));
            }

            if(!WorldUtils.destroyClaimer(pos)) {
                WarringStatesMod.getLogger().warn("No TileEntityClaimer found at position " + pos);
            }
        });

        return null;
    }
}
