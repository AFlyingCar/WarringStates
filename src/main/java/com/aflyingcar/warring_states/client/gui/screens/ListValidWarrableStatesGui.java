package com.aflyingcar.warring_states.client.gui.screens;

import com.aflyingcar.warring_states.WarringStatesNetwork;
import com.aflyingcar.warring_states.client.gui.parts.ListGui;
import com.aflyingcar.warring_states.client.gui.screens.ConfirmActionGui;
import com.aflyingcar.warring_states.network.messages.DeclareWarOnStateMessage;
import com.aflyingcar.warring_states.states.DummyState;
import com.aflyingcar.warring_states.util.GuiUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ListValidWarrableStatesGui extends GuiScreen {
    private final GuiScreen parent;
    private final EntityPlayer player;
    private final Map<DummyState, Integer> allvalidWarrableStates;
    private ListGui<DummyState> warrableStates;

    public ListValidWarrableStatesGui(EntityPlayer player, GuiScreen parent, Map<DummyState, Integer> allValidWarrableStates) {
        this.player = player;
        this.parent = parent;
        this.allvalidWarrableStates = allValidWarrableStates;
    }


    @Override
    public void initGui() {
        buttonList.clear();

        warrableStates = new ListGui<>(fontRenderer, this, mc, width, height, new ArrayList<>(allvalidWarrableStates.keySet()), this::confirmDeclareWar, (element, i) -> element.getName(), null);

        warrableStates.registerScrollButtons(7, 8);
    }

    protected void confirmDeclareWar(DummyState state, boolean isDoubleClick, int mouseX, int mouseY) {
        mc.displayGuiScreen(new ConfirmActionGui(this, player, () -> {
            WarringStatesNetwork.NETWORK.sendToServer(new DeclareWarOnStateMessage(state.getUUID(), player.getPersistentID()));
            mc.displayGuiScreen(parent);
        }, null, "warring_states.gui.management.declare_war", state.getName()));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.enableBlend();
        drawDefaultBackground();

        // TODO: draw GUI background
        drawString(fontRenderer, GuiUtils.translate("warrable_states"), width / 2 - GuiUtils.getTranslatedStringWidth(fontRenderer, "warrable_states"), height / 4, 0xFFFFFF);

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
