package com.aflyingcar.warring_states.api;

import com.aflyingcar.warring_states.WarringStatesConfig;
import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.events.WargoalDeclaredEvent;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import com.aflyingcar.warring_states.util.ChunkGroup;
import com.aflyingcar.warring_states.util.ExtendedBlockPos;
import com.aflyingcar.warring_states.util.PlayerUtils;
import com.aflyingcar.warring_states.util.WorldUtils;
import com.aflyingcar.warring_states.war.WarManager;
import com.aflyingcar.warring_states.war.goals.StealChunkWarGoal;
import com.aflyingcar.warring_states.war.goals.WarGoalFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class WarringStatesAPI {
    public static boolean doesPlayerHavePermissionForAction(@Nonnull World world, @Nonnull UUID uuid, @Nonnull BlockPos position, @Nullable EnumFacing side, int actionPrivileges) {
        return doesPlayerHavePermissionForAction(world, uuid, WorldUtils.offsetBlockPos(position, side), actionPrivileges);
    }

    public static boolean doesPlayerHavePermissionForAction(@Nonnull World world, @Nonnull UUID uuid, @Nonnull BlockPos position, int actionPrivileges) {
        State state = StateManager.getInstance().getStateAtPosition(world, position);
        State playerState = StateManager.getInstance().getStateFromPlayerUUID(uuid);

        return doesPlayerHavePermissionForAction(world, uuid, actionPrivileges, state, playerState);
    }

    public static boolean doesPlayerHavePermissionForAction(@Nonnull World world, @Nonnull UUID uuid, int actionPrivileges, @Nullable State state, @Nullable State playerState) {
        // We don't actually know on the client, so we'll assume that they have permission until the server tells us otherwise
        if(world.isRemote) return true;

        // We have permissions to do the action if
        //  a) This chunk is not claimed
        //  b) We have the privilege to perform this action in this State's territory
        //  c) We are at war with this State
        if(state == null) {
            // return playerState == null || playerState.hasPrivilege(uuid, actionPrivileges);
            return true;
        }

        // If this we are trying to do this in our own territory first
        //  Just check to see if the player has privileges to perform this action
        if(state.equals(playerState)) {
            return state.hasPrivilege(uuid, actionPrivileges);
        } else {
            // If we are in somebody else's  territory, we need to be at war to INTERACT and MODIFY_BLOCKS
            if((actionPrivileges & (CitizenPrivileges.INTERACT | CitizenPrivileges.MODIFY_BLOCKS)) != 0) {
                return WarManager.getInstance().isAtWarWith(uuid, state);
            } else {
                if(playerState != null) {
                    // If we are in a state ourselves, make sure our state gives us permission to do this
                    return playerState.hasPrivilege(uuid, actionPrivileges);
                } else {
                    // Otherwise, we have no permissions to do anything in this person's territory
                    return false;
                }
            }
        }
    }

    @SideOnly(Side.SERVER)
    public static boolean isPositionInClaimedTerritory(World world, BlockPos position) {
        return StateManager.getInstance().getStateAtPosition(world, position) != null;
    }

    @SideOnly(Side.SERVER)
    public static void claimChunkForState(State state, ChunkPos pos, int dimension) {
        state.claimTerritory(pos, dimension);
    }

    @SideOnly(Side.SERVER)
    public static void declareWarOn(@Nonnull EntityPlayer player, @Nonnull State belligerent, @Nonnull State target) {
        if(!belligerent.hasPrivilege(player.getPersistentID(), CitizenPrivileges.DECLARE_WAR)) {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.declare_war.permission_denied", belligerent.getName()));
            return;
        }

        if(WarManager.getInstance().isAtWar(player)) {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.already_at_war"));
            return;
        }

        if(!WarManager.getInstance().canParticipateInWar(belligerent)) {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.belligerent_cannot_participate_in_war", WarManager.getInstance().getRemainingWarWaitTimerHoursFor(belligerent)));
            return;
        }

        if(!WarManager.getInstance().canParticipateInWar(target)) {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.target_cannot_participate_in_war", target.getName(), WarManager.getInstance().getRemainingWarWaitTimerHoursFor(target)));
            return;
        }

        // Sanity check: players who are not a citizen of belligerent cannot declare war for belligerent
        if(!belligerent.hasCitizen(player.getPersistentID())) {
            WarringStatesMod.getLogger().error("Tried to declare war between '" + belligerent.getName() + "' and '" + target.getName() + "', but player " + player.getName() + " is not a citizen of the belligerent.");
            return;
        }

        // Get the wargoals that belligerent has against target
        Set<IWarGoal> goalsSet = belligerent.getWargoalsAgainst(target.getUUID());

        // Cannot declare war without at least one wargoal
        if(goalsSet == null || goalsSet.isEmpty()) {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.need_war_reason"));
            return;
        }

        // Convert the Set into a NonNullList
        NonNullList<IWarGoal> goals = goalsSet.stream().collect(Collectors.toCollection(NonNullList::create));

        // Actually create the war
        WarManager.getInstance().startWar(belligerent, goals, target);
    }

    @SideOnly(Side.SERVER)
    public static void claimStealChunkWargoal(@Nonnull EntityPlayer player, @Nonnull State futureBelligerent, @Nonnull State currentOwner, @Nonnull ChunkPos chunkPos, int dimension) {
        if(WarManager.getInstance().isAtWarWith(futureBelligerent, currentOwner)) {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.cannnot_claim_from_enemy"));
            return;
        }

        ChunkGroup capital = currentOwner.getCapital();
        if(capital != null && capital.containsChunk(chunkPos, dimension)) {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.cannot_claim_capital"));
            return;
        }

        if(futureBelligerent.equals(currentOwner)) {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.cannot_steal_own_territory"));
            return;
        }

        StealChunkWarGoal goal = Objects.requireNonNull((StealChunkWarGoal) WarGoalFactory.newWargoal(WarGoalFactory.Goals.STEAL_CHUNK));

        goal.setChunk(chunkPos);
        goal.setDimension(dimension);

        if(!goal.canBeDeclared(player)) {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.cannot_declare", "Steal Chunk"));
            return;
        }

        // Allow the wargoal event to be explicitly intercepted and cancelled if necessary
        if(!MinecraftForge.EVENT_BUS.post(new WargoalDeclaredEvent(goal))) {
            futureBelligerent.declareWargoal(currentOwner.getUUID(), goal);

            player.sendMessage(new TextComponentString("Claimed chunk " + chunkPos + " as a wargoal for " + futureBelligerent.getName()));
        } else {
            WarringStatesMod.getLogger().info("Wargoal declaration cancelled.");
        }
    }

    @SideOnly(Side.SERVER)
    public static void dissolveState(@Nonnull State state, @Nonnull EntityPlayer player) {
        if(!WarringStatesConfig.allowDissolvingStatesWithMembers && state.getCitizens().size() > 1) {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.server_disallows_dissolving_states_with_members"));

            return;
        }

        StateManager.getInstance().removeState(state);

        int dimID = WorldUtils.getDimensionIDForWorld((WorldServer)player.getEntityWorld());

        for(ChunkPos pos : state.getClaimedTerritory()) {
            // Find every tile entity that has the same UUID as the given state and destroy it
            //noinspection ResultOfMethodCallIgnored
            WorldUtils.getTileEntitiesWithinChunk(TileEntityClaimer.class, player.getEntityWorld(), pos).stream().filter(te -> Objects.equals(te.getStateUUID(), state.getUUID())).map(TileEntityClaimer::getPos).map(p -> new ExtendedBlockPos(p, dimID)).map(WorldUtils::destroyClaimer);
        }
    }

    public static void acceptCitizenshipApplication(@Nonnull State state, @Nonnull UUID playerID, @Nonnull UUID applierPlayerID) {
        String acceptorName = PlayerUtils.getPlayerNameFromUUID(playerID);
        EntityPlayer appliedPlayer = PlayerUtils.getPlayerByUUID(applierPlayerID);

        if(state.acceptApplicationFor(applierPlayerID)) {
            if(appliedPlayer != null) {
                appliedPlayer.sendMessage(new TextComponentTranslation("warring_states.messages.application.accepted", state.getName(), acceptorName == null ? "" : acceptorName));
            }
            state.getCitizens().stream().map(PlayerUtils::getPlayerByUUID).filter(Objects::nonNull).forEach(e -> e.sendMessage(new TextComponentTranslation("warring_states.messages.new_citizen", PlayerUtils.getPlayerNameFromUUID(applierPlayerID), state.getName())));

            WarringStatesMod.getLogger().info("Accepted application for " + applierPlayerID + " to " + state.getName());
        } else {
            WarringStatesMod.getLogger().warn("Failed to accept application for " + applierPlayerID);
        }
    }

    public static void rejectCitizenshipApplication(State state, UUID playerID, UUID applierPlayerID) {
        String rejectorName = PlayerUtils.getPlayerNameFromUUID(playerID);
        EntityPlayer appliedPlayer = PlayerUtils.getPlayerByUUID(applierPlayerID);

        state.rejectApplicationFor(applierPlayerID);
        if(appliedPlayer != null) {
            appliedPlayer.sendMessage(new TextComponentTranslation("warring_states.messages.application.rejected", state.getName(), rejectorName == null ? "" : rejectorName));
        }
        WarringStatesMod.getLogger().info("Rejected application for " + applierPlayerID + " to " + state.getName());
    }
}
