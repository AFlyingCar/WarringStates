package com.aflyingcar.warring_states.network.handlers.client;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.client.gui.GuiID;
import com.aflyingcar.warring_states.network.messages.OpenGuiMessage;
import com.aflyingcar.warring_states.util.NetworkUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class OpenGuiHandler implements IMessageHandler<OpenGuiMessage, IMessage> {
    @Override
    public IMessage onMessage(OpenGuiMessage message, MessageContext ctx) {
        NetworkUtils.getThreadListener(ctx).addScheduledTask(() -> WarringStatesMod.proxy.openGUI(message.getPos(), GuiID.fromInt(message.getGuiID()), message.getPrivileges()));

        return null;
    }
}
