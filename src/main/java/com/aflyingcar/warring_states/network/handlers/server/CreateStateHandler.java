package com.aflyingcar.warring_states.network.handlers.server;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.network.messages.CreateStateMessage;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import com.aflyingcar.warring_states.util.ExtendedBlockPos;
import com.aflyingcar.warring_states.util.NetworkUtils;
import com.aflyingcar.warring_states.util.WorldUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CreateStateHandler implements IMessageHandler<CreateStateMessage, IMessage> {
    @Override
    public IMessage onMessage(CreateStateMessage message, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> {
            ExtendedBlockPos capital = message.getCapital();

            // Cancel the creation of a state, and destroy the claimer block
            if(message.isCanceled()) {
                if(!WorldUtils.destroyClaimer(capital)) {
                    WarringStatesMod.getLogger().warn("No tile entity was destroyed!");
                }

                return;
            }

            TileEntity te = WorldUtils.getTileEntityAtExtendedPosition(capital);
            if(te instanceof TileEntityClaimer) {
                ((TileEntityClaimer)te).changeOwner(message.getName(), message.getDesc(), message.getStateID());
            }

            State state = StateManager.getInstance().newState(message.getName(), message.getDesc(), message.getStateID(), message.getFounder());
            state.claimTerritory(WorldUtils.getChunkFor(capital).getPos(), capital.getDimID());
        });

        return null;
    }
}
