package com.aflyingcar.warring_states.handlers;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.WarringStatesNetwork;
import com.aflyingcar.warring_states.client.gui.GuiID;
import com.aflyingcar.warring_states.client.gui.screens.ConfirmApplyForCitizenshipGui;
import com.aflyingcar.warring_states.client.gui.screens.DiplomacyGui;
import com.aflyingcar.warring_states.client.gui.screens.StateCreationGui;
import com.aflyingcar.warring_states.client.gui.screens.StateManagementGui;
import com.aflyingcar.warring_states.network.messages.OpenGuiMessage;
import com.aflyingcar.warring_states.states.DummyState;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import com.aflyingcar.warring_states.util.ExtendedBlockPos;
import com.aflyingcar.warring_states.util.WorldUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        // TODO: We should probably do this in a switch statement, but all GUIs currently use the same system
        WarringStatesNetwork.NETWORK.sendTo(new OpenGuiMessage(ID, new BlockPos(x, y, z), StateManager.getInstance().getPrivilegesFor(player)), (EntityPlayerMP) player);

        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        ExtendedBlockPos pos = new ExtendedBlockPos(x, y, z, player.dimension);
        DummyState dummyState = WorldUtils.getStateInfoAtPosition(world, pos);

        switch(GuiID.fromInt(ID)) {
            case STATE_CREATION_GUI:
                return new StateCreationGui((TileEntityClaimer) world.getTileEntity(pos), player);
            case STATE_MANAGER_GUI:
                if(dummyState == null) {
                    WarringStatesMod.getLogger().warn("Cannot open Management GUI at " + pos + ", as we were unable to find any State information there.");
                    return null;
                }
                return new StateManagementGui(dummyState, player, pos);
            case CONFIRM_APPLY_CITIZENSHIP:
                if(dummyState == null) {
                    WarringStatesMod.getLogger().warn("Cannot open Application GUI at " + pos + ", as we were unable to find any State information there.");
                    return null;
                }
                return new ConfirmApplyForCitizenshipGui(dummyState.getName(), dummyState.getUUID(), player);
            case DIPLOMACY_GUI:
                return new DiplomacyGui(); // TODO
            default:
                return null;
        }
    }
}
