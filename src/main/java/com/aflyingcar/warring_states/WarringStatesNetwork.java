package com.aflyingcar.warring_states;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class WarringStatesNetwork {
    private static int discriminator = 0;

    public static SimpleNetworkWrapper NETWORK;

    public static void preinit() {
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(WarringStatesMod.MOD_ID);
    }

    public static <REQ extends IMessage, REPLY extends IMessage> void registerServerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType) {
        NETWORK.registerMessage(messageHandler, requestMessageType, discriminator, Side.SERVER);
        ++discriminator;
    }

    public static <REQ extends IMessage, REPLY extends IMessage> void registerClientMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType) {
        NETWORK.registerMessage(messageHandler, requestMessageType, discriminator, Side.CLIENT);
        ++discriminator;
    }
}
