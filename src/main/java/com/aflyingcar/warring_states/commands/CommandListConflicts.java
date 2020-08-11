package com.aflyingcar.warring_states.commands;

import com.aflyingcar.warring_states.war.Conflict;
import com.aflyingcar.warring_states.war.WarManager;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandListConflicts extends CommandBase {
    @Override
    public String getName() {
        return "listConflicts";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/listConflicts";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        List<Conflict> conflicts = WarManager.getInstance().getAllConflicts();

        int i = 0;
        for(Conflict conflict : conflicts) {
            sender.sendMessage(new TextComponentString(i + ": " + conflict.toString()));
            ++i;
        }
    }
}
