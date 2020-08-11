package com.aflyingcar.warring_states.client.gui.screens;

import com.aflyingcar.warring_states.client.gui.parts.ListGui;
import com.aflyingcar.warring_states.war.DummyConflict;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public class ListCurrentConflictsGui extends GuiScreen {
    private final GuiScreen parent;
    private final List<DummyConflict> conflicts;
    private ListGui<DummyConflict> listGui;

    public ListCurrentConflictsGui(@Nonnull GuiScreen parent, @Nonnull List<DummyConflict> conflicts) {
        this.parent = parent;
        this.conflicts = conflicts;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        // TODO: Do we want any tooltips???
        listGui = new ListGui<>(fontRenderer, this, mc, width, height, conflicts,
                                (element, b, i1, i2) -> mc.displayGuiScreen(new ShowConflictInformationGui(this, element)),
                                (element, i) -> "Conflict #" + i, null);

        // TODO: This is done by GuiLanguage, but do we actually need to do it?
        listGui.registerScrollButtons(7, 8);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.enableBlend();
        drawDefaultBackground();

        // TODO: draw GUI background

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
