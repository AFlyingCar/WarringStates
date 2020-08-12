package com.aflyingcar.warring_states.commands;

import com.aflyingcar.warring_states.states.StateManager;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSanityCheckControlledTerritory extends CommandBase {
    @Override
    public String getName() {
        return "sanityCheckControlledTerritory";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/sanityCheckControlledTerritory <fixProblems?>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        boolean fixProblems = args.length == 1 && Boolean.parseBoolean(args[0]);

        StateManager.getInstance().getStates().forEach(s -> s.sanityCheckAllClaimedTerritory(fixProblems));
    }
}
