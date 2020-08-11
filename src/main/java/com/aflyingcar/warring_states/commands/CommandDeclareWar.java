package com.aflyingcar.warring_states.commands;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.WarringStatesAPI;
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
public class CommandDeclareWar extends CommandBase {
    @Override
    public String getName() {
        return "declareWar";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/declareWar <targetState>";
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

        String targetName = args[0];

        State target = StateManager.getInstance().getStateFromName(targetName);

        if(target == null) {
            sender.sendMessage(new TextComponentString("Cannot find the state named '" + targetName + "'"));
            return;
        }

        State senderState = StateManager.getInstance().getStateFromPlayer((EntityPlayer)sender);

        if(senderState == null) {
            sender.sendMessage(new TextComponentString("You are not in a state, so you cannot declare war on somebody else."));
            return;
        }

        WarringStatesAPI.declareWarOn((EntityPlayer)sender, senderState, target);
    }
}
