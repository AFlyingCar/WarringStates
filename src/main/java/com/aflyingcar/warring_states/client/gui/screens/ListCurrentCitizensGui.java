package com.aflyingcar.warring_states.client.gui.screens;

import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.client.gui.parts.ListGui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ListCurrentCitizensGui extends GuiScreen {
    private final GuiScreen parent;
    private final Map<String, Integer> citizenNamePrivilegeMap;
    private ListGui<String> citizenListGui;

    public ListCurrentCitizensGui(@Nullable GuiScreen parent, @Nonnull Map<String, Integer> citizenNamePrivilegeMap) {
        this.parent = parent;
        this.citizenNamePrivilegeMap = citizenNamePrivilegeMap;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        // TODO: Should this GUI actually be used for modifying individual citizen privilege levels?
        citizenListGui = new ListGui<>(fontRenderer, this, mc, width, height, new ArrayList<>(citizenNamePrivilegeMap.keySet()), null, (element, i) -> element, (element) -> "Privilege level of " + CitizenPrivileges.toString(citizenNamePrivilegeMap.get(element)));

        citizenListGui.registerScrollButtons(7, 8);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.enableBlend();
        drawDefaultBackground();

        // TODO: draw GUI background

        citizenListGui.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.disableBlend();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        citizenListGui.actionPerformed(button);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        citizenListGui.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
        }
    }
}
