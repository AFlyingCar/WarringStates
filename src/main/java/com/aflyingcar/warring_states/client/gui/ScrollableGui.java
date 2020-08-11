package com.aflyingcar.warring_states.client.gui;

import com.aflyingcar.warring_states.client.gui.parts.ListGui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

public abstract class ScrollableGui<T> extends GuiScreen {
    private final GuiScreen parent;
    private final List<T> list;
    private ListGui<T> listGui;

    public ScrollableGui(GuiScreen parent, List<T> list) {
        this.parent = parent;
        this.list = list;
    }

    protected abstract void onElementClicked(T element, boolean doubleClicked, int mouseX, int mouseY);
    protected abstract String getElementString(T element, int index);
    protected abstract String getTooltipText(T element);
    protected abstract void drawGuiBackground();

    @Override
    public void initGui() {
        buttonList.clear();

        listGui = new ListGui<>(fontRenderer, this, mc, width, height, list, this::onElementClicked, this::getElementString, this::getTooltipText);

        listGui.registerScrollButtons(7, 8);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.enableBlend();
        drawDefaultBackground();

        drawGuiBackground();

        listGui.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.disableBlend();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        listGui.actionPerformed(button);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        listGui.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
        }
    }
}
