package com.aflyingcar.warring_states.commands;

import com.aflyingcar.warring_states.events.WarCompleteEvent;
import com.aflyingcar.warring_states.war.Conflict;
import com.aflyingcar.warring_states.war.WarManager;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandForceWinner extends CommandBase {
    @Override
    public String getName() {
        return "forceWinner";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/forceWinner <conflictID> <sideID>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendMessage(new TextComponentString("Invalid number of arguments"));
            return;
        }

        int conflictID = Integer.parseInt(args[0]);
        int sideID = Integer.parseInt(args[1]);

        Conflict conflict = WarManager.getInstance().getConflictForID(conflictID);

        if(conflict == null) {
            sender.sendMessage(new TextComponentString("No such conflict for ID " + conflictID));
            return;
        }
        if(sideID != 0 && sideID != 1) {
            sender.sendMessage(new TextComponentString("sideID must be either 0 or 1, not " + sideID + "!"));
            return;
        }

        Conflict.Side side = Conflict.Side.values()[sideID];

        conflict.forceSetWinner(side);

        WarCompleteEvent event = new WarCompleteEvent(conflict);
        MinecraftForge.EVENT_BUS.post(event);

        if(!event.isCanceled()) {
            WarManager.getInstance().getAllConflicts().remove(conflictID);
            WarManager.getInstance().markDirty();
        }
    }
}
