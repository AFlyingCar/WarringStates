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
import java.util.Objects;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandSetCapital extends CommandBase {
    @Override
    public String getName() {
        return "setCapital";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/setCapital";
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
            sender.sendMessage(new TextComponentString("No state owns the current chunk. Capital cannot be set here."));
            return;
        }

        State state = StateManager.getInstance().getStateFromPlayer((EntityPlayer)sender);

        if(!Objects.equals(state, owningState) || !WarringStatesAPI.doesPlayerHavePermissionForAction(world, senderUUID, position, CitizenPrivileges.MANAGEMENT)) {
            sender.sendMessage(new TextComponentString("This chunk must be owned by your state, and you must have sufficient permissions to manage your state."));
            return;
        }

        owningState.setCapital(chunkPos, WorldUtils.getDimensionIDForWorld((WorldServer)world));
    }
}
