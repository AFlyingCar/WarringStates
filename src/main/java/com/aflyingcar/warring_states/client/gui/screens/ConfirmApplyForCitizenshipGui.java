package com.aflyingcar.warring_states.client.gui.screens;

import com.aflyingcar.warring_states.WarringStatesNetwork;
import com.aflyingcar.warring_states.network.messages.CitizenAppliesMessage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class ConfirmApplyForCitizenshipGui extends GuiScreen {
    private final String stateName;
    private final UUID stateID;
    private final EntityPlayer player;

    public ConfirmApplyForCitizenshipGui(String stateName, @Nonnull UUID stateID, EntityPlayer player) {
        this.stateName = stateName;
        this.stateID = stateID;
        this.player = player;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.mc.displayGuiScreen(new GuiYesNo(this, I18n.format("warring_states.gui.state.citizenship_apply", stateName), "", 0));
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if(result) {
            // NOTE: We don't actually apply here, we send it up to the server so we can apply on the server
            // state.apply(player.getPersistentID());
            // StateManager.getInstance().markDirty();

            // Also fire an event for the client-side
            // MinecraftForge.EVENT_BUS.post(new CitizenApplicationEvent(state, player.getPersistentID()));
            WarringStatesNetwork.NETWORK.sendToServer(new CitizenAppliesMessage(player.getPersistentID(), stateID));
        }

        player.closeScreen();
    }
}
