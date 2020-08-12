package com.aflyingcar.warring_states.commands;

import com.aflyingcar.warring_states.war.WarManager;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandClearAllRestorationOperations extends CommandBase {
    @Override
    public String getName() {
        return "clearAllRestorationOperations";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/clearAllRestorationOperations";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        WarManager.getInstance().clearRollbackOperations();
    }
}
