package com.aflyingcar.warring_states.war.goals;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.events.WargoalDeclaredEvent;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.util.ChunkGroup;
import com.aflyingcar.warring_states.war.WarManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class DefaultWargoalClaimers {
    @SideOnly(Side.SERVER)
    public static void claimStealChunkWargoal(EntityPlayer player, State futureBelligerent, State currentOwner, ChunkPos chunkPos, int dimension) {
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
            player.sendMessage(new TextComponentTranslation("warring_states.messages.cannot_claim_own_goal", WarGoalFactory.Goals.STEAL_CHUNK.name()));
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
    public static void claimRaidWargoal(EntityPlayer player, State futureBelligerent, State targetState, World world) {
        if(WarManager.getInstance().isAtWarWith(futureBelligerent, targetState)) {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.cannnot_claim_from_enemy"));
            return;
        }

        if(futureBelligerent.equals(targetState)) {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.cannot_claim_own_goal", WarGoalFactory.Goals.RAID.name()));
            return;
        }

        RaidWarGoal goal = Objects.requireNonNull((RaidWarGoal) WarGoalFactory.newWargoal(WarGoalFactory.Goals.RAID));

        if(!goal.canBeDeclared(player)) {
            player.sendMessage(new TextComponentTranslation("warring_states.messages.cannot_declare", "Raid"));
            return;
        }

        if(!MinecraftForge.EVENT_BUS.post(new WargoalDeclaredEvent(goal))) {
            futureBelligerent.declareWargoal(targetState.getUUID(), goal);

            player.sendMessage(new TextComponentTranslation("warring_states.messages.claimed_raid_wargoal", targetState.getName()));
        } else {
            WarringStatesMod.getLogger().info("Wargoal declaration cancelled.");
        }
    }
}
