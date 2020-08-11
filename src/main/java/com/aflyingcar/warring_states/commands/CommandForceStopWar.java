package com.aflyingcar.warring_states.commands;

import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.war.Conflict;
import com.aflyingcar.warring_states.war.WarManager;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandForceStopWar extends CommandBase {
    @Override
    public String getName() {
        return "forceStopWar";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/forceStopWar [<state1Name> <state2Name>|<conflictID>]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if(args.length != 2 && args.length != 1) {
            sender.sendMessage(new TextComponentString("Invalid number of arguments"));
            return;
        }

        int conflictID;
        if(args.length == 2) {
            String state1Name = args[0];
            String state2Name = args[1];

            State s1 = StateManager.getInstance().getStateFromName(state1Name);
            State s2 = StateManager.getInstance().getStateFromName(state2Name);

            Pair<Integer, Conflict> conflict = WarManager.getInstance().getConflictBetween(s1, s2);

            if(conflict == null) {
                sender.sendMessage(new TextComponentString("No conflict found between " + state1Name + " and " + state2Name));
                return;
            }
            conflictID = conflict.getLeft();
        } else {
            conflictID = Integer.parseInt(args[0]);

            if(WarManager.getInstance().getConflictForID(conflictID) == null) {
                sender.sendMessage(new TextComponentString("No conflict for ID #" + conflictID));
                return;
            }
        }

        WarManager.getInstance().forceStopConflict(conflictID);
    }
}
