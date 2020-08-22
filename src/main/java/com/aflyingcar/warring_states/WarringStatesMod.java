package com.aflyingcar.warring_states;

import com.aflyingcar.warring_states.blocks.BlockClaimer;
import com.aflyingcar.warring_states.common.CommonProxy;
import com.aflyingcar.warring_states.handlers.GuiHandler;
import com.aflyingcar.warring_states.items.ItemFlagBase;
import com.aflyingcar.warring_states.items.ItemFlagPole;
import com.aflyingcar.warring_states.items.ItemWargoalClaimer;
import com.aflyingcar.warring_states.network.handlers.client.*;
import com.aflyingcar.warring_states.network.handlers.server.*;
import com.aflyingcar.warring_states.network.messages.*;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import com.aflyingcar.warring_states.war.WarManager;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = WarringStatesMod.MOD_ID,
        name = WarringStatesMod.MOD_NAME,
        version = WarringStatesMod.VERSION
)
public class WarringStatesMod {
    public static final String MOD_ID = "warring_states";
    public static final String MOD_NAME = "Warring States";
    public static final String VERSION = "1.0.0";

    private static Logger logger;

    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @SuppressWarnings("unused")
    @Mod.Instance(MOD_ID)
    public static WarringStatesMod INSTANCE;
    private static Side side;

    @SidedProxy(clientSide = "com.aflyingcar.warring_states.client.ClientProxy", serverSide = "com.aflyingcar.warring_states.common.CommonProxy")
    public static CommonProxy proxy;

    public static Logger getLogger() {
        return logger;
    }

    public static Side getSide() {
        return side;
    }

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        side = event.getSide();

        proxy.preinit();

        GameRegistry.registerTileEntity(TileEntityClaimer.class, new ResourceLocation(MOD_ID, BlockClaimer.NAME));

        WarringStatesNetwork.preinit();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

        // Register all network packets and handlers
        WarringStatesNetwork.registerServerMessage(UpdateStateInfoHandler.class, UpdateStateInfoMessage.class);
        WarringStatesNetwork.registerServerMessage(CreateStateHandler.class, CreateStateMessage.class);
        WarringStatesNetwork.registerServerMessage(CitizenAppliesHandler.class, CitizenAppliesMessage.class);
        WarringStatesNetwork.registerServerMessage(RevokeCitizenshipHandler.class, RevokeCitizenshipMessage.class);
        WarringStatesNetwork.registerServerMessage(RescindTerritoryClaimHandler.class, RescindTerritoryClaimMessage.class);
        WarringStatesNetwork.registerServerMessage(RequestConflictListHandler.class, RequestConflictListMessage.class);
        WarringStatesNetwork.registerServerMessage(RequestStateCitizenListHandler.class, RequestStateCitizenListMessage.class);
        WarringStatesNetwork.registerServerMessage(RequestAllValidWarrableStatesHandler.class, RequestAllValidWarrableStatesMessage.class);
        WarringStatesNetwork.registerServerMessage(DeclareWarOnStateHandler.class, DeclareWarOnStateMessage.class);
        WarringStatesNetwork.registerServerMessage(CitizenApplicationResultHandler.class, CitizenApplicationResultMessage.class);
        WarringStatesNetwork.registerServerMessage(DissolveStateHandler.class, DissolveStateMessage.class);
        WarringStatesNetwork.registerServerMessage(RequestStateCitizenApplicationListHandler.class, RequestStateCitizenApplicationListMessage.class);
        WarringStatesNetwork.registerServerMessage(UpdatePlayerPrivilegesHandler.class, UpdatePlayerPrivilegesMessage.class);
        WarringStatesNetwork.registerServerMessage(KickPlayerFromStateHandler.class, KickPlayerFromStateMessage.class);
        WarringStatesNetwork.registerServerMessage(MoveCapitalToHandler.class, MoveCapitalToMessage.class);
        WarringStatesNetwork.registerServerMessage(RequestFullStateInformationHandler.class, RequestFullStateInformationMessage.class);

        WarringStatesNetwork.registerClientMessage(OpenGuiHandler.class, OpenGuiMessage.class);
        WarringStatesNetwork.registerClientMessage(DeliverConflictListHandler.class, DeliverConflictListMessage.class);
        WarringStatesNetwork.registerClientMessage(DeliverStateCitizenListHandler.class, DeliverStateCitizenListMessage.class);
        WarringStatesNetwork.registerClientMessage(DeliverAllValidWarrableStatesHandler.class, DeliverAllValidWarrableStatesMessage.class);
        WarringStatesNetwork.registerClientMessage(DeliverStateCitizenApplicationListHandler.class, DeliverStateCitizenApplicationListMessage.class);
        WarringStatesNetwork.registerClientMessage(DeliverFullStateInformationHandler.class, DeliverFullStateInformationMessage.class);

        if(side == Side.SERVER) {
            StateManager.getInstance().setSide(side = event.getSide());
        }
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        proxy.postinit();
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        proxy.initializeManagers(event.getServer());

        proxy.registerServerCommands(event);
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        if(side == Side.SERVER) {
            // Immediately begin all decay timers, as no player will actually be logged in for this
            //  The timers will begin to accumulate until somebody logs in
            StateManager.getInstance().getStates().forEach(State::startDecayTimer);
        }
    }

    // TODO: Do we actually need this? Since we handle WorldEvent.Save in StateEventsHandler, and that should get called
    //   on exit anyway
    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        proxy.saveManagers();
    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        if(side == Side.SERVER) {
            StateManager.getInstance().resetAllData();
            WarManager.getInstance().resetAllData();
        }
    }

    /**
     * Forge will automatically look up and bind blocks to the fields in this class
     * based on their registry name.
     */
    @SuppressWarnings("unused")
    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Blocks {
          public static final BlockClaimer BLOCK_CLAIMER = null; // placeholder for special block below
    }

    /**
     * This is a special class that listens to registry events, to allow creation of mod blocks and items at the proper time.
     */
    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler {
        /**
         * Listen for the register event for creating custom items
         */
        @SubscribeEvent
        public static void addItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().register(new ItemBlock(WarringStatesBlocks.BLOCK_CLAIMER).setRegistryName(MOD_ID, BlockClaimer.NAME));
            event.getRegistry().register(new ItemWargoalClaimer());
            event.getRegistry().register(new ItemFlagBase());
            event.getRegistry().register(new ItemFlagPole());
        }

        /**
         * Listen for the register event for creating custom blocks
         */
        @SubscribeEvent
        public static void addBlocks(RegistryEvent.Register<Block> event) {
             event.getRegistry().register(WarringStatesBlocks.BLOCK_CLAIMER);
        }

        @SubscribeEvent
        public static void addModels(ModelRegistryEvent event) {
            ModelLoader.setCustomModelResourceLocation(WarringStatesItems.FLAG_POLE, 0, new ModelResourceLocation(WarringStatesItems.FLAG_POLE.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(WarringStatesItems.FLAG_BASE, 0, new ModelResourceLocation(WarringStatesItems.FLAG_BASE.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(WarringStatesItems.WARGOAL_CLAIMER, 0, new ModelResourceLocation(WarringStatesItems.WARGOAL_CLAIMER.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(WarringStatesItems.CLAIMER, 0, new ModelResourceLocation(WarringStatesItems.CLAIMER.getRegistryName(), "inventory"));
        }
    }
}
