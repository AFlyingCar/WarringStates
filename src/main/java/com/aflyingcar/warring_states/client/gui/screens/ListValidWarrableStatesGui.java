package com.aflyingcar.warring_states.client.gui.screens;

import com.aflyingcar.warring_states.WarringStatesNetwork;
import com.aflyingcar.warring_states.client.gui.parts.ListGui;
import com.aflyingcar.warring_states.network.messages.DeclareWarOnStateMessage;
import com.aflyingcar.warring_states.util.GuiUtils;
import com.aflyingcar.warring_states.util.WarrableState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

public class ListValidWarrableStatesGui extends GuiScreen {
    private final GuiScreen parent;
    private final EntityPlayer player;
    private final List<WarrableState> allvalidWarrableStates;
    private ListGui<WarrableState> warrableStates;

    public ListValidWarrableStatesGui(EntityPlayer player, GuiScreen parent, List<WarrableState> allValidWarrableStates) {
        this.player = player;
        this.parent = parent;
        this.allvalidWarrableStates = allValidWarrableStates;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        warrableStates = new ListGui<>(fontRenderer, this, mc, width, height,
                                       allvalidWarrableStates, this::confirmDeclareWar, (element, i) -> element.getTargetStateName(), null);

        warrableStates.registerScrollButtons(7, 8);
    }

    protected void confirmDeclareWar(WarrableState state, boolean isDoubleClick, int mouseX, int mouseY) {
        mc.displayGuiScreen(new ConfirmDeclareWarGui(this, player, () -> {
            WarringStatesNetwork.NETWORK.sendToServer(new DeclareWarOnStateMessage(state.getTargetStateID(), player.getPersistentID()));
            mc.displayGuiScreen(parent);
        }, null, state.getTargetStateName()));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.enableBlend();
        drawDefaultBackground();

        // TODO: draw GUI background
        drawString(fontRenderer, GuiUtils.translate("warrable_states"), width / 2 - GuiUtils.getTranslatedStringWidth(fontRenderer, "warrable_states") / 2, warrableStates.top - 20, 0xFFFFFF);

        warrableStates.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.disableBlend();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        warrableStates.actionPerformed(button);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        warrableStates.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
        }
    }
}
