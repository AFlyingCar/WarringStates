package com.aflyingcar.warring_states.commands;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.api.WarringStatesAPI;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.WorldUtils;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandClaimWargoal extends CommandBase {
    @Override
    public String getName() {
        return "claimWargoal";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/claimWargoal";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if(!(sender instanceof EntityPlayer)) {
            WarringStatesMod.getLogger().error("Non-players may not use this command");
            return;
        }

        World world = ((EntityPlayer) sender).world;
        UUID senderUUID = ((EntityPlayer) sender).getPersistentID();
        BlockPos position = sender.getPosition();

        ChunkPos chunkPos = world.getChunk(position).getPos();

        State owningState = StateManager.getInstance().getStateAtPosition(world, position);

        if(owningState == null) {
            sender.sendMessage(new TextComponentString("No state owns the current chunk. No wargoal can be declared."));
            return;
        }

        State state = StateManager.getInstance().getStateFromPlayer((EntityPlayer)sender);

        if(state == null || !WarringStatesAPI.doesPlayerHavePermissionForAction(world, senderUUID, position, CitizenPrivileges.DECLARE_WAR)) {
            sender.sendMessage(new TextComponentString("You are not a part of a state or do not have sufficient permissions to declare wargoals."));
            return;
        }

        WarringStatesAPI.claimStealChunkWargoal((EntityPlayer) sender, state, owningState, chunkPos, WorldUtils.getDimensionIDForWorld((WorldServer) world));
    }
}
