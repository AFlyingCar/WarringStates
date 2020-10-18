package com.aflyingcar.warring_states.client.gui.screens;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.WarringStatesNetwork;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.network.messages.*;
import com.aflyingcar.warring_states.states.DummyState;
import com.aflyingcar.warring_states.util.ChunkGroup;
import com.aflyingcar.warring_states.util.ExtendedBlockPos;
import com.aflyingcar.warring_states.util.GuiUtils;
import com.aflyingcar.warring_states.util.NetworkUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class StateManagementGui extends GuiScreen {
    private static final ResourceLocation GUI_BACKGROUND = new ResourceLocation(WarringStatesMod.MOD_ID, "textures/gui/state_management.png");

    private final EntityPlayer player;
    private final int playerPrivileges;
    private final ExtendedBlockPos pos; // The position of the TileEntity that is being interacted with

    private final int textureWidth = 300;
    private final int textureHeight = 200;

    private int posX = 0;
    private int posY = 0;

    private DummyState state;

    private boolean isMoveCapitolEnabled;
    private boolean initialized = false;
    private boolean receivedFullStateInfo = false;

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

    private void disableButton(GuiButton button) {
        button.enabled = false;
        button.packedFGColour = 0;
    }

    private void enableButton(GuiButton button) {
        button.enabled = true;
        button.packedFGColour = 14737632; // TODO
    }

    private void changeMoveCapitolEnabled(boolean enabled) {
        isMoveCapitolEnabled = enabled;

        if(!initialized) return;

        if(enabled) {
            enableButton(buttonList.get(ButtonID.MOVE_CAPITAL.ordinal()));
        } else {
            disableButton(buttonList.get(ButtonID.MOVE_CAPITAL.ordinal()));
        }
    }

    public StateManagementGui(DummyState state, EntityPlayer player, ExtendedBlockPos pos) {
        this.state = state;
        this.pos = pos;
        this.playerPrivileges = WarringStatesMod.proxy.getPrivilegesFor(player.getPersistentID());

        NetworkUtils.sendTrackedMessage(new RequestFullStateInformationMessage(state.getUUID()), (message) -> {
            this.state = ((DeliverFullStateInformationMessage)message).getState();
            ChunkGroup capitol = this.state.getCapital();
            changeMoveCapitolEnabled((capitol == null) || !capitol.containsBlock(pos));

            this.receivedFullStateInfo = true;
        });

        this.player = player;
    }

    protected boolean playerHasPrivilegesForButton(ButtonID buttonID) {
        return (playerPrivileges & buttonID.getPrivilegeMask()) == buttonID.getPrivilegeMask();
    }

    @Override
    public void initGui() {
        buttonList.clear();

        posX = (this.width - textureWidth) / 2;
        posY = (this.height - textureHeight) / 2 + 20*2;

        int largestWidth = 0;
        for(ButtonID buttonID : ButtonID.values()) {
            int newWidth = GuiUtils.getTranslatedStringWidth(fontRenderer, buttonID.getKey());
            if(newWidth > largestWidth)
                largestWidth = newWidth;

            GuiButton button = addButton(new GuiButton(buttonID.ordinal(), 0, 0, 0, 20, buttonID.translate(state.getName())));
            // Does this person not have sufficient privileges to access this feature
            if(!playerHasPrivilegesForButton(buttonID)) {
                disableButton(button);
            }
        }

        int centeredButtonX = GuiUtils.getPositionToCenterElementOnTexture(width, textureWidth, largestWidth);

        // Treat Leave State specially since it should be centered
        buttonList.get(0).width = largestWidth;
        buttonList.get(0).x = centeredButtonX;
        buttonList.get(0).y = this.posY;

        boolean left = true;
        int posY = this.posY + 20 + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE; // One down from Leave State
        for(int i = 1; i < buttonList.size() - 1; ++i) { // Skip Leave State and Dissolve State
            GuiButton button = buttonList.get(i);
            int posX = left ? this.posX : this.posX + buttonList.get(i - 1).width + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE;
            button.width = largestWidth;
            button.x = posX + 10;
            button.y = posY;

            posY += left ? 0 : 20 + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE;

            left = !left;
        }

        GuiButton dissolveButton = buttonList.get(ButtonID.DISSOLVE_STATE.ordinal());
        dissolveButton.width = largestWidth;
        dissolveButton.x = centeredButtonX;
        dissolveButton.y = posY + (left ? 0 : 20 + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE);

        // Only color it RED if the player has privileges for this action
        if(playerHasPrivilegesForButton(ButtonID.DISSOLVE_STATE))
            dissolveButton.packedFGColour = 0xFF0000;

        // Special case here in case the state information gets back to us before we finish properly initializing
        if(!isMoveCapitolEnabled) {
            disableButton(buttonList.get(ButtonID.MOVE_CAPITAL.ordinal()));
        }

        initialized = true;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Don't draw anything at all if we have yet to receive all of the state information
        if(!receivedFullStateInfo) return;

        // Draws slightly darkened background
        drawDefaultBackground();

        this.mc.renderEngine.bindTexture(GUI_BACKGROUND);

        this.posX = (this.width - textureWidth) / 2;
        this.posY = (this.height - textureHeight) / 2;

        // TODO: Draw GUI window
        GlStateManager.pushMatrix();
        GlStateManager.scale(2, 2, 1);
        drawTexturedModalRect(posX / 2, posY / 2, 0, 0, textureWidth / 2, textureHeight / 2);
        GlStateManager.popMatrix();

        int titleX = GuiUtils.getPositionToCenterTextOnTexture(width, textureWidth, fontRenderer, "management_title", state.getName());
        drawString(fontRenderer, GuiUtils.translate("management_title", state.getName()), titleX, posY + 8, 0xFFFFFF);

        int descX = GuiUtils.getPositionToCenterStringOnTexture(width, textureWidth, fontRenderer, state.getDesc());
        drawString(fontRenderer, state.getDesc(), descX, posY + 20 + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE, 0xFFFFFF);

        // Draw buttons and stuff
        super.drawScreen(mouseX, mouseY, partialTicks);

        drawTooltips(mouseX, mouseY);
    }

    protected void drawTooltips(int mouseX, int mouseY) {
        String text = getButtonTooltipText(getCurrentHoveredButton());

        if(text != null) {
            drawHoveringText(text, mouseX, mouseY);
        }
    }

    @Nullable
    protected String getButtonTooltipText(@Nullable ButtonID buttonID) {
        if(buttonID == null) return null;

        if(!playerHasPrivilegesForButton(buttonID)) {
            return I18n.format("warring_states.tooltip.not_enough_privileges", CitizenPrivileges.toString(buttonID.getPrivilegeMask()));
        }

        switch(buttonID) {
            case MOVE_CAPITAL:
                if(!isMoveCapitolEnabled)
                    return I18n.format("warring_states.tooltip.move_capital_not_available");
            case DISSOLVE_STATE:
                // TODO: This text should be colored red
                return I18n.format("warring_states.tooltip.dissolve_cannot_be_undone");
            case MANAGE_APPLICATIONS:
                return I18n.format("warring_states.tooltip.manage_applications", "" + state.getAllApplications().size());
            case REVOKE_CITIZENSHIP:
                return I18n.format("warring_states.tooltip.revoke_citizenship");
            case DECLARE_WAR:
                if(state.isAtWar()) {
                    return I18n.format("warring_states.tooltip.already_at_war");
                } else {
                    return I18n.format("warring_states.tooltip.declare_war", "" + state.getWargoalTargets().size());
                }
            case MANAGE_CITIZENS:
            case RESCIND_CLAIM:
            case CHANGE_NAME:
            case CHANGE_DESC:
            case VIEW_CURRENT_CONFLICTS:
            default:
                return null;
        }
    }

    @Nullable
    protected ButtonID getCurrentHoveredButton() {
        for(int i = 0; i < buttonList.size(); ++i) {
            GuiButton button = buttonList.get(i);
            if(button.visible && button.isMouseOver()) {
                return ButtonID.fromInt(i);
            }
        }

        return null;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        ButtonID id = ButtonID.fromInt(button.id);
        if(id == null) return;

        switch(id) {
            case REVOKE_CITIZENSHIP:
                mc.displayGuiScreen(new ConfirmActionGui(this, player, () -> {
                    WarringStatesNetwork.NETWORK.sendToServer(new RevokeCitizenshipMessage(state.getUUID(), player.getPersistentID()));
                    player.closeScreen();
                }, null, "warring_states.gui." + id.getKey() + "_confirmation", state.getName()));
                break;
            case MANAGE_APPLICATIONS:
                NetworkUtils.sendTrackedMessage(new RequestStateCitizenApplicationListMessage(state.getUUID()), message -> mc.displayGuiScreen(new ListCurrentCitizenApplicationsGui(this, state.getUUID(), player, ((DeliverStateCitizenApplicationListMessage)message).getCitizenApplications())));
                break;
            case MANAGE_CITIZENS:
                NetworkUtils.sendTrackedMessage(new RequestStateCitizenListMessage(state.getUUID()), message -> mc.displayGuiScreen(new ListCitizenManagementGui(this, state.getUUID(), player, playerPrivileges, ((DeliverStateCitizenListMessage)message).getCitizenList())));
                break;
            case RESCIND_CLAIM:
                mc.displayGuiScreen(new ConfirmActionGui(this, player, () -> WarringStatesNetwork.NETWORK.sendToServer(new RescindTerritoryClaimMessage(state.getUUID(), pos, player.getPersistentID())), null, "warring_states.gui." + id.getKey() + "_confirmation", state.getName()));
                break;
            case DECLARE_WAR:
                // TODO: We should have a tooltip for this button if this State is already at war to admonish them for being such a warmonger
                NetworkUtils.sendTrackedMessage(new RequestAllValidWarrableStatesMessage(state.getUUID(), player.getPersistentID()), message -> mc.displayGuiScreen(new ListValidWarrableStatesGui(player, this, ((DeliverAllValidWarrableStatesMessage)message).getWarrableStates())));
                break;
            case CHANGE_NAME:
                mc.displayGuiScreen(new EditStringGui(this, player, newName -> {
                    state.setName(newName);
                    WarringStatesNetwork.NETWORK.sendToServer(new UpdateStateInfoMessage(player.getPersistentID(), state.getUUID(), state.getName()));
                }, id.getKey(), state.getName()));
                break;
            case CHANGE_DESC:
                mc.displayGuiScreen(new EditStringGui(this, player, newDesc -> {
                    state.setDescription(newDesc);
                    WarringStatesNetwork.NETWORK.sendToServer(new UpdateStateInfoMessage(player.getPersistentID(), state.getUUID(), state.getName(), state.getDesc()));
                }, id.getKey(), state.getDesc()));
                break;
            case DISSOLVE_STATE:
                mc.displayGuiScreen(new ConfirmActionGui(this, player, () -> WarringStatesNetwork.NETWORK.sendToServer(new DissolveStateMessage(state.getUUID(), player.getPersistentID())), null, "warring_states.gui." + id.getKey() + "_confirmation", state.getName()));
                break;
            case VIEW_CURRENT_CONFLICTS:
                NetworkUtils.sendTrackedMessage(new RequestConflictListMessage(), message -> mc.displayGuiScreen(new ListCurrentConflictsGui(this, ((DeliverConflictListMessage)message).getConflicts())));
                break;
            case MOVE_CAPITAL:
                mc.displayGuiScreen(new ConfirmActionGui(this, player, () -> {
                    WarringStatesNetwork.NETWORK.sendToServer(new MoveCapitalToMessage(state.getUUID(), player.getPersistentID(), pos));
                    enableButton(buttonList.get(ButtonID.MOVE_CAPITAL.ordinal()));
                }, null, "warring_states.gui." + id.getKey() + "_confirmation"));
                break;
        }
    }
}
