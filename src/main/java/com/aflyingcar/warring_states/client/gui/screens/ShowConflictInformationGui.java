package com.aflyingcar.warring_states.client.gui.screens;

import com.aflyingcar.warring_states.states.DummyState;
import com.aflyingcar.warring_states.util.GuiUtils;
import com.aflyingcar.warring_states.war.DummyConflict;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class ShowConflictInformationGui extends GuiScreen {
    private final GuiScreen parent;
    private final DummyConflict conflict;


    public ShowConflictInformationGui(@Nullable GuiScreen parent, @Nonnull DummyConflict conflict) {
        this.parent = parent;
        this.conflict = conflict;
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // TODO: Draw progress bar at the top to show who is most winning
        //  2 colors for the bar (similar to HoI4), with progress towards completing all of their wargoals
        // TODO: Draw all belligerents side by side with the defenders
        int posX = width / 4;
        int posY = height / 16;

        drawString(fontRenderer, GuiUtils.translate("belligerents"), posX, 0, 0xFFFFFF);
        for(DummyState state : conflict.getBelligerents().keySet()) {
            drawString(fontRenderer, state.getName(), posX, posY, 0xFFFFFF);
            posY += 20 + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE;
        }

        posY = height / 16;
        posX = posX * 3; // render at 3/4 of the width
        drawString(fontRenderer, GuiUtils.translate("defenders"), posX, 0, 0xFFFFFF);
        for(DummyState state : conflict.getDefenders().keySet()) {
            drawString(fontRenderer, state.getName(), posX, posY, 0xFFFFFF);
            posY += 20 + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            if(parent != null) {
                mc.displayGuiScreen(parent);
            }
        }
    }
}
