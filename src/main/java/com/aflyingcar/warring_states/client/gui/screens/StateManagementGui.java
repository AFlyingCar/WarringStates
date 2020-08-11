package com.aflyingcar.warring_states.client.gui.screens;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.WarringStatesNetwork;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.network.messages.*;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.util.ExtendedBlockPos;
import com.aflyingcar.warring_states.util.GuiUtils;
import com.aflyingcar.warring_states.util.NetworkUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.UUID;

public class StateManagementGui extends GuiScreen {
    private final UUID stateID;
    private String currentName;
    private String currentDesc;
    private final EntityPlayer player;
    private final int playerPrivileges;
    private final ExtendedBlockPos pos; // The position of the TileEntity that is being interacted with

    private final int textureWidth = 150;
    private final int textureHeight = 150;

    private int posX = 0;
    private int posY = 0;

    private enum ButtonID {
        REVOKE_CITIZENSHIP,
        MANAGE_APPLICATIONS,
        MANAGE_CITIZENS,
        RESCIND_CLAIM,
        DECLARE_WAR,
        CHANGE_NAME,
        CHANGE_DESC,
        VIEW_CURRENT_CONFLICTS,
        MOVE_CAPITAL,
        DISSOLVE_STATE,
        ;

        private static final String[] translationKeys = {
            "revoke_citizenship",
            "manage_applications",
            "manage_citizens",
            "rescind_claim",
            "declare_war",
            "change_name",
            "change_desc",
            "view_current_conflicts",
            "move_capital",
            "dissolve_state",
        };

        private static final int[] privilegeMapping = {
                CitizenPrivileges.NONE,
                CitizenPrivileges.RECRUITMENT,
                CitizenPrivileges.NONE,
                CitizenPrivileges.CLAIM_TERRITORY,
                CitizenPrivileges.DECLARE_WAR,
                CitizenPrivileges.MANAGEMENT,
                CitizenPrivileges.MANAGEMENT,
                CitizenPrivileges.NONE,
                CitizenPrivileges.MANAGEMENT,
                CitizenPrivileges.ALL,
        };

        @Nullable
        public static ButtonID fromInt(int id) {
            return id >= 0 && id < values().length ? values()[id] : null;
        }

        public String getKey() {
            return translationKeys[ordinal()];
        }

        public String translate(Object... args) {
            return GuiUtils.translate(getKey(), args);
        }

        public int getPrivilegeMask() {
            return privilegeMapping[ordinal()];
        }
    }

    public StateManagementGui(State state, EntityPlayer player, ExtendedBlockPos pos) {
        this.stateID = state.getUUID();
        this.currentName = state.getName();
        this.currentDesc = state.getDesc();
        this.pos = pos;
        this.playerPrivileges = WarringStatesMod.proxy.getPrivilegesFor(player.getPersistentID());

        this.player = player;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        posX = (this.width - textureWidth) / 4;
        posY = (this.height - textureHeight) / 2 + 20*2;

        int largestWidth = 0;
        for(ButtonID buttonID : ButtonID.values()) {
            int newWidth = GuiUtils.getTranslatedStringWidth(fontRenderer, buttonID.getKey());
            if(newWidth > largestWidth)
                largestWidth = newWidth;

            GuiButton button = addButton(new GuiButton(buttonID.ordinal(), 0, 0, 0, 20, buttonID.translate()));
            // Does this person not have sufficient privileges to access this feature
            if((playerPrivileges & buttonID.getPrivilegeMask()) != buttonID.getPrivilegeMask()) {
                button.enabled = false;
                button.packedFGColour = 0;
            }
        }

        // Treat Leave State specially since it should be centered
        buttonList.get(0).width = largestWidth;
        buttonList.get(0).x = posX * 2;// - (largestWidth / 2);
        buttonList.get(0).y = this.posY;

        boolean left = true;
        int posY = this.posY + 20 + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE; // One down from Leave State
        for(int i = 1; i < buttonList.size() - 1; ++i) { // Skip Leave State and Dissolve State
            GuiButton button = buttonList.get(i);
            int posX = left ? this.posX : this.posX + buttonList.get(i - 1).width + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE;
            button.width = largestWidth;
            button.x = posX;
            button.y = posY;

            posY += left ? 0 : 20 + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE;

            left = !left;
        }

        GuiButton dissolveButton = buttonList.get(ButtonID.DISSOLVE_STATE.ordinal());
        dissolveButton.width = largestWidth;
        dissolveButton.x = posX * 2;// - (largestWidth  / 2);
        dissolveButton.y = posY + (left ? 0 : 20 + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draws slightly darkened background
        drawDefaultBackground();

        // TODO
        // this.mc.renderEngine.bindTexture(new ResourceLocation(WarringStatesMod.MOD_ID, "wtf???"));

        this.posX = (this.width - textureWidth) / 2;
        this.posY = (this.height - textureHeight) / 2;

        // TODO: Draw GUI window
        drawTexturedModalRect(posX, posY, 0, 0, textureWidth, textureHeight);

        drawString(fontRenderer, GuiUtils.translate("management_title", currentName), posX, posY, 0xFFFFFF);
        drawString(fontRenderer, currentDesc, posX, posY + 20 + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE, 0xFFFFFF);

        // Draw buttons and stuff
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        ButtonID id = ButtonID.fromInt(button.id);
        if(id == null) return;

        switch(id) {
            case REVOKE_CITIZENSHIP:
                mc.displayGuiScreen(new ConfirmActionGui(this, player, () -> {
                    WarringStatesNetwork.NETWORK.sendToServer(new RevokeCitizenshipMessage(stateID, player.getPersistentID()));
                    player.closeScreen();
                }, null, "warring_states.gui." + id.getKey() + "_confirmation", currentName));
                break;
            case MANAGE_APPLICATIONS:
                NetworkUtils.sendTrackedMessage(new RequestStateCitizenApplicationListMessage(stateID), message -> mc.displayGuiScreen(new ListCurrentCitizenApplicationsGui(this, stateID, player, ((DeliverStateCitizenApplicationListMessage)message).getCitizenApplications())));
                break;
            case MANAGE_CITIZENS:
                NetworkUtils.sendTrackedMessage(new RequestStateCitizenListMessage(stateID), message -> mc.displayGuiScreen(new ListCitizenManagementGui(this, stateID, player, playerPrivileges, ((DeliverStateCitizenListMessage)message).getCitizenList())));
                break;
            case RESCIND_CLAIM:
                mc.displayGuiScreen(new ConfirmActionGui(this, player, () -> WarringStatesNetwork.NETWORK.sendToServer(new RescindTerritoryClaimMessage(stateID, pos, player.getPersistentID())), null, "warring_states.gui." + id.getKey() + "_confirmation", currentName));
                break;
            case DECLARE_WAR:
                // TODO: We should have a tooltip for this button if this State is already at war to admonish them for being such a warmonger
                NetworkUtils.sendTrackedMessage(new RequestAllValidWarrableStatesMessage(stateID, player.getPersistentID()), message -> mc.displayGuiScreen(new ListValidWarrableStatesGui(player, this, ((DeliverAllValidWarrableStatesMessage)message).getWargoals())));
                break;
            case CHANGE_NAME:
                mc.displayGuiScreen(new EditStringGui(this, player, newName -> {
                    currentName = newName;
                    WarringStatesNetwork.NETWORK.sendToServer(new UpdateStateInfoMessage(player.getPersistentID(), stateID, currentName));
                }, id.getKey(), currentName));
                break;
            case CHANGE_DESC:
                mc.displayGuiScreen(new EditStringGui(this, player, newDesc -> {
                    currentDesc = newDesc;
                    WarringStatesNetwork.NETWORK.sendToServer(new UpdateStateInfoMessage(player.getPersistentID(), stateID, currentName, currentDesc));
                }, id.getKey(), currentDesc));
                break;
            case DISSOLVE_STATE:
                mc.displayGuiScreen(new ConfirmActionGui(this, player, () -> WarringStatesNetwork.NETWORK.sendToServer(new DissolveStateMessage(stateID, player.getPersistentID())), null, "warring_states.gui." + id.getKey() + "_confirmation", currentName));
                break;
            case VIEW_CURRENT_CONFLICTS:
                NetworkUtils.sendTrackedMessage(new RequestConflictListMessage(), message -> mc.displayGuiScreen(new ListCurrentConflictsGui(this, ((DeliverConflictListMessage)message).getConflicts())));
                break;
            case MOVE_CAPITAL:
                mc.displayGuiScreen(new ConfirmActionGui(this, player, () -> WarringStatesNetwork.NETWORK.sendToServer(new MoveCapitalToMessage(stateID, player.getPersistentID(), pos)), null, "warring_states.gui." + id.getKey() + "_confirmation"));
                break;
        }
    }
}
