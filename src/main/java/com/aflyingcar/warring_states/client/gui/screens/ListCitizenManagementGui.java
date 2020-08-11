package com.aflyingcar.warring_states.client.gui.screens;

import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.client.gui.parts.ListGui;
import com.aflyingcar.warring_states.states.DummyCitizen;
import com.aflyingcar.warring_states.util.GuiUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class ListCitizenManagementGui extends GuiScreen {
    private final GuiScreen parent;
    private final UUID stateID;
    private final EntityPlayer player;
    private final int playerPrivileges;
    private final List<DummyCitizen> citizens;
    private ListGui<DummyCitizen> citizenGui;

    public ListCitizenManagementGui(GuiScreen parent, UUID stateID, EntityPlayer player, int playerPrivileges, List<DummyCitizen> citizens) {
        this.parent = parent;
        this.stateID = stateID;
        this.player = player;
        this.playerPrivileges = playerPrivileges;
        this.citizens = citizens;
    }

    protected boolean hasManagementPrivileges() {
        return (playerPrivileges & CitizenPrivileges.MANAGEMENT) == CitizenPrivileges.MANAGEMENT;
    }

    protected void onElementClicked(DummyCitizen citizen, boolean doubleClicked, int mouseX, int mouseY) {
        if(hasManagementPrivileges()) {
            mc.displayGuiScreen(new ManageStateCitizenGui(this, player, stateID, citizen));
        }
    }

    public void removeCitizen(DummyCitizen citizen) {
        citizens.remove(citizen);
    }

    @Override
    public void initGui() {
        buttonList.clear();

        // TODO: Should this GUI actually be used for modifying individual citizen privilege levels?
        citizenGui = new ListGui<>(fontRenderer, this, mc, width, height, citizens, this::onElementClicked, (element, i) -> element.getName(),
                (element) -> "Privilege level of " + CitizenPrivileges.toString(element.getPrivileges()));

        citizenGui.registerScrollButtons(7, 8);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.enableBlend();
        drawDefaultBackground();

        // TODO: draw GUI background
        drawString(fontRenderer, GuiUtils.translate("citizen_management"), width / 2 - GuiUtils.getTranslatedStringWidth(fontRenderer, "citizen_management") / 2, height / 16, 0xFFFFFF);

        citizenGui.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.disableBlend();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        citizenGui.actionPerformed(button);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        citizenGui.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
        }
    }
}
