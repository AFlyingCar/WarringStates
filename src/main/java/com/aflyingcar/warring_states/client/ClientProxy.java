package com.aflyingcar.warring_states.client;

import com.aflyingcar.warring_states.client.gui.GuiID;
import com.aflyingcar.warring_states.client.tile.TileEntityClaimerRenderer;
import com.aflyingcar.warring_states.common.CommonProxy;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import com.aflyingcar.warring_states.util.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ClientProxy extends CommonProxy {
    private static final Map<UUID, Integer> playerPrivilegeMapping = new HashMap<>();

    @Override
    public void preinit() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityClaimer.class, new TileEntityClaimerRenderer());
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void postinit() {
        super.postinit();
    }

    @Override
    public void openGUI(BlockPos pos, GuiID guiID, int privileges) {
        playerPrivilegeMapping.put(Minecraft.getMinecraft().player.getPersistentID(), privileges);
        GuiUtils.openGUI(Minecraft.getMinecraft().player, pos, guiID);
    }

    @Override
    public int getPrivilegesFor(UUID playerUUID) {
        return playerPrivilegeMapping.getOrDefault(playerUUID, 0);
    }

    @Override
    public void initializeManagers(MinecraftServer server) { }

    @Override
    public void saveManagers() { }

    @Override
    public void markStateManagerDirty() {
    }

    @Override
    public void registerServerCommands(FMLServerStartingEvent event) {
    }

    @Override
    public EntityPlayer getPlayerForUUID(UUID playerUUID) {
        return Minecraft.getMinecraft().player;
    }

    @Override
    public boolean isPlayerOnline(UUID playerUUID) {
        return true;
    }

    @Override
    public void registerDefaultFlyingCheckAndConsumers() {
    }

    @Override
    public void registerFlyingCheckAndStopper(Predicate<EntityPlayer> isFlyingPredicate, Consumer<EntityPlayer> stopFlyingConsumer) {
    }

    @Override
    public boolean isFlying(EntityPlayer player) {
        return false;
    }

    @Override
    public void stopFlying(EntityPlayer p) {
    }
}
