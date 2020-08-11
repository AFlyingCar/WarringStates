package com.aflyingcar.warring_states.commands;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandJoinState extends CommandBase {
    @Override
    public String getName() {
        return "joinState";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/joinState <stateName>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if(args.length < 1) {
            sender.sendMessage(new TextComponentString("Invalid number of arguments"));
            return;
        }

        if(!(sender instanceof EntityPlayer)) {
            WarringStatesMod.getLogger().error("Non-players may not use this command");
            return;
        }

        if(StateManager.getInstance().getStateFromPlayer((EntityPlayer)sender) != null) {
            sender.sendMessage(new TextComponentString("Cannot join a state while you are in one."));
            return;
        }

        String stateName = args[0];

        State state = StateManager.getInstance().getStateFromName(stateName);

        if(state == null) {
            sender.sendMessage(new TextComponentString("Unknown state '" + stateName + '\''));
        } else {
            state.addCitizen(((EntityPlayer) sender).getPersistentID(), 0);
        }
    }
}
