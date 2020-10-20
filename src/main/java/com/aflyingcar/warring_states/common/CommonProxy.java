package com.aflyingcar.warring_states.common;

import com.aflyingcar.warring_states.WarringStatesConfig;
import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.WarringStatesAPI;
import com.aflyingcar.warring_states.client.gui.GuiID;
import com.aflyingcar.warring_states.commands.*;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.PlayerUtils;
import com.aflyingcar.warring_states.util.WorldUtils;
import com.aflyingcar.warring_states.war.WarManager;
import com.aflyingcar.warring_states.war.goals.DefaultWargoalClaimers;
import com.aflyingcar.warring_states.war.goals.WarGoalFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemElytra;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("EmptyMethod")
public class CommonProxy {
    private List<Predicate<EntityPlayer>> isFlyingPredicates = new ArrayList<>();
    private List<Consumer<EntityPlayer>> stopFlyingConsumers = new ArrayList<>();
    private List<Predicate<Item>> doesItemAllowFlightPredicates = new ArrayList<>();

    public void preinit() {
        if(WarringStatesConfig.shouldBlockModificationOfTileEntitiesBeIgnoredDuringWar) {
            // We want to ignore block placing and breaking during a war for anything that has a tile entity
            WarManager.getInstance().registerIgnoreBlockBreakPredicate(p -> {
                World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(p.getDimID());
                //noinspection ConstantConditions
                if(world == null) {
                    WarringStatesMod.getLogger().warn("Got a null world for dimension ID " + p.getDimID());
                    return false;
                }

                return world.getBlockState(p).getBlock().hasTileEntity(world.getBlockState(p));
            });

            WarManager.getInstance().registerIgnoreBlockPlacePredicate(p -> {
                World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(p.getDimID());
                //noinspection ConstantConditions
                if(world == null) {
                    WarringStatesMod.getLogger().warn("Got a null world for dimension ID " + p.getDimID());
                    return false;
                }

                return world.getBlockState(p).getBlock().hasTileEntity(world.getBlockState(p));
            });
        }

        registerDefaultWargoalClaimers();
    }

    public void init() {
        registerDefaultFlyingCheckAndConsumers();
        registerDefaultDoesItemAllowFlightPredicates();
    }

    public void postinit() { }

    public void openGUI(BlockPos pos, GuiID guiID, int privileges) { }

    public void initializeManagers(MinecraftServer server) {
        // Go ahead and just use the overworld dimension, as that is basically the root of the save folder anyway
        File saveLocation = server.getWorld(0).getChunkSaveLocation();
        StateManager.getInstance().setRootGameDirectory(saveLocation);
        WarManager.getInstance().setRootGameDirectory(saveLocation);

        StateManager.getInstance().loadInfoFromFile();
        WarManager.getInstance().loadInfoFromFile();
    }

    public void saveManagers() {
        StateManager.getInstance().writeInfoToFile();
        WarManager.getInstance().writeInfoToFile();
    }

    public void markStateManagerDirty() {
        StateManager.getInstance().markDirty();
    }

    public void registerServerCommands(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandGivePrivileges());
        event.registerServerCommand(new CommandJoinState());
        event.registerServerCommand(new CommandClaimWargoal());
        event.registerServerCommand(new CommandDeclareWar());
        event.registerServerCommand(new CommandForceStopWar());
        event.registerServerCommand(new CommandListConflicts());
        event.registerServerCommand(new CommandSetCapital());
        event.registerServerCommand(new CommandForceWinner());
        event.registerServerCommand(new CommandClearAllRestorationOperations());
        event.registerServerCommand(new CommandSanityCheckControlledTerritory());
    }

    public void registerDefaultFlyingCheckAndConsumers() {
        registerFlyingCheckAndStopper(player -> player.capabilities.isFlying, player -> player.capabilities.isFlying = false);
    }

    public void registerDefaultDoesItemAllowFlightPredicates() {
        registerDoesItemAllowFlightPredicate(ItemElytra.class::isInstance);
    }

    public void registerDefaultWargoalClaimers() {
        WarringStatesAPI.registerWargoalClaimer(WarGoalFactory.Goals.STEAL_CHUNK.ordinal(), (player, playerState, targetState, world) -> {
            ChunkPos pos = world.getChunk(player.getPosition()).getPos();
            DefaultWargoalClaimers.claimStealChunkWargoal(player, playerState, targetState, pos, WorldUtils.getDimensionIDForWorld(world));

            return true;
        });

        WarringStatesAPI.registerWargoalClaimer(WarGoalFactory.Goals.RAID.ordinal(), ((player, playerState, targetState, world) -> {
            DefaultWargoalClaimers.claimRaidWargoal(player, playerState, targetState, world);
            return true;
        }));
    }

    public int getPrivilegesFor(UUID playerUUID) {
        return StateManager.getInstance().getPrivilegesFor(playerUUID);
    }

    public EntityPlayer getPlayerForUUID(UUID playerUUID) {
        return PlayerUtils.getPlayerByUUID(playerUUID);
    }

    public boolean isPlayerOnline(UUID playerUUID) {
        return PlayerUtils.isPlayerOnline(playerUUID);
    }

    public void registerFlyingCheckAndStopper(Predicate<EntityPlayer> isFlyingPredicate, Consumer<EntityPlayer> stopFlyingConsumer) {
        isFlyingPredicates.add(isFlyingPredicate);
        stopFlyingConsumers.add(stopFlyingConsumer);
    }

    public void registerDoesItemAllowFlightPredicate(Predicate<Item> predicate) {
        doesItemAllowFlightPredicates.add(predicate);
    }

    public boolean isFlying(EntityPlayer player) {
        return isFlyingPredicates.stream().anyMatch(p -> p.test(player));
    }

    public void stopFlying(EntityPlayer p) {
        stopFlyingConsumers.forEach(c -> c.accept(p));
    }

    public boolean doesItemAllowFlight(Item item) {
        return doesItemAllowFlightPredicates.stream().anyMatch(p -> p.test(item));
    }
}
