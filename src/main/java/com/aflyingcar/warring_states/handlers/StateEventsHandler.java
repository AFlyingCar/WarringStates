package com.aflyingcar.warring_states.handlers;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.IWarGoal;
import com.aflyingcar.warring_states.api.WarringStatesAPI;
import com.aflyingcar.warring_states.events.*;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import com.aflyingcar.warring_states.util.PlayerUtils;
import com.aflyingcar.warring_states.util.WorldUtils;
import com.aflyingcar.warring_states.war.Conflict;
import com.aflyingcar.warring_states.war.WarManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = WarringStatesMod.MOD_ID)
public class StateEventsHandler {
    @SubscribeEvent
    public static void handleWorldSave(WorldEvent.Save event) {
        WarringStatesMod.proxy.saveManagers();
    }

    @SubscribeEvent
    public static void onWarDeclared(WarDeclaredEvent event) {
        Conflict war = event.getWar();

        Set<UUID> defenders = war.getDefenders().keySet().stream().map(State::getUUID).collect(Collectors.toSet());
        Set<UUID> belligerents = war.getBelligerents().keySet().stream().map(State::getUUID).collect(Collectors.toSet());

        for(State belligerent : war.getBelligerents().keySet()) {
            belligerent.onWarDeclaredOn(defenders);
        }

        for(State defender : war.getDefenders().keySet()) {
            defender.onWarDeclaredBy(belligerents);
        }
    }

    @SubscribeEvent
    public static void onWarComplete(WarCompleteEvent event) {
        Conflict war = event.getWar();
        war.getWarStartTimer().stop(); // Stop the wargoal timer

        // Get all the UUIDs of both the defenders and belligerents
        Set<UUID> defenders = war.getDefenders().keySet().stream().map(State::getUUID).collect(Collectors.toSet());
        Set<UUID> belligerents = war.getBelligerents().keySet().stream().map(State::getUUID).collect(Collectors.toSet());

        Conflict.Side winner = war.getWinner();

        if(winner == Conflict.Side.BELLIGERENT) {
            for(UUID player : war.getBelligerents().keySet().stream().map(State::getCitizens).flatMap(Collection::stream).collect(Collectors.toList())) {
                EntityPlayerMP playerEntity = PlayerUtils.getPlayerByUUID(player);
                if(playerEntity != null) {
                    playerEntity.sendMessage(new TextComponentString("The belligerents have won!"));
                }
            }
            for(UUID player : war.getDefenders().keySet().stream().map(State::getCitizens).flatMap(Collection::stream).collect(Collectors.toList())) {
                EntityPlayerMP playerEntity = PlayerUtils.getPlayerByUUID(player);
                if(playerEntity != null) {
                    playerEntity.sendMessage(new TextComponentString("The belligerents have won!"));
                }
            }

            for(Map.Entry<State, NonNullList<IWarGoal>> belligerent : war.getBelligerents().entrySet()) {
                // Winner is the belligerents, tell them that they have won
                belligerent.getKey().onWarWon(defenders);
                belligerent.getValue().forEach(goal -> goal.onSuccess(belligerent.getKey()));
            }
            for(Map.Entry<State, NonNullList<IWarGoal>> defender : war.getDefenders().entrySet()) {
                // Defenders lost, let them know this
                defender.getKey().onWarLost(belligerents);
            }
        } else {
            for(Map.Entry<State, NonNullList<IWarGoal>> belligerent : war.getBelligerents().entrySet()) {
                // Belligerents lost, let them know this
                belligerent.getKey().onWarLost(defenders);
            }
            for(Map.Entry<State, NonNullList<IWarGoal>> defender : war.getDefenders().entrySet()) {
                // Winner is the defenders, tell them that they have won
                defender.getKey().onWarWon(belligerents);
                defender.getValue().forEach(goal -> goal.onSuccess(defender.getKey()));
            }
        }

        war.rollbackChanges();

        WarManager.getInstance().startWarWaitTimers(war);
    }

    @SubscribeEvent
    public static void onTerritoryClaimed(TerritoryClaimedEvent event) {
        WarringStatesAPI.claimChunkForState(event.getState(), event.getWorld().getChunk(event.getPos()).getPos(), WorldUtils.getDimensionIDForWorld((WorldServer)event.getWorld()));
    }

    @SubscribeEvent
    public static void onChunkCaptureBeginEvent(ChunkCaptureBeginEvent event) {
        Conflict conflict = event.getConflict();
        Conflict.Side attackerSide = conflict.getSideOf(event.getCapturerState());
        if(attackerSide == null) {
            WarringStatesMod.getLogger().error("ChunkCaptureBegin occurred from state " + event.getCapturerState().getName() + ", but we could not determine which side of the conflict that state is on.");
            return;
        }

        BlockPos pos = event.getClaimer().getPos();
        String posString = "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";

        Set<State> chunkDefenderStates;
        switch(attackerSide) {
            case DEFENDER:
                chunkDefenderStates = conflict.getBelligerents().keySet();
                break;
            case BELLIGERENT:
                chunkDefenderStates = conflict.getDefenders().keySet();
                break;
            default:
                return;
        }

        chunkDefenderStates.stream().map(State::getCitizens).flatMap(Collection::stream).map(PlayerUtils::getPlayerByUUID).filter(Objects::nonNull).forEach(player -> player.sendMessage(new TextComponentTranslation("warring_states.messages.chunk_capture_beginning", posString)));
    }

    @SubscribeEvent
    public static void onChunkCaptureCompleteEvent(ChunkCaptureCompleteEvent event) {
        if(WarringStatesMod.getSide() == Side.CLIENT) {
            // TODO
        } else {
            TileEntityClaimer claimer = event.getCapturedClaimer();

            String stateName = claimer.getStateName();

            State state = StateManager.getInstance().getStateFromName(stateName);

            List<Conflict> conflicts = WarManager.getInstance().getAllConflictsInvolving(state);

            for(Conflict conflict : conflicts) {
                conflict.captureClaimer(event.getCapturedClaimer());
            }

            claimer.onCapture(conflicts);
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        StateManager.getInstance().update();
        WarManager.getInstance().update();
    }
}
