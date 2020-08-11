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
public class CommandGivePrivileges extends CommandBase {
    @Override
    public String getName() {
        return "givePrivileges";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/givePrivileges <String:StateName> <int:privilegeLevel>";
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

        String sprivilegeLevel = args[0];

        int privilegeLevel = Integer.parseInt(sprivilegeLevel);

        State state = StateManager.getInstance().getStateFromPlayer((EntityPlayer) sender);

        if(state == null) {
            sender.sendMessage(new TextComponentString("You are not a part of a state, and thus cannot be given permissions."));
        } else {
            state.setPrivileges(((EntityPlayer)sender).getPersistentID(), privilegeLevel);
        }
    }
}
