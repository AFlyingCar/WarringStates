package com.aflyingcar.warring_states.client.gui.screens;

import com.aflyingcar.warring_states.WarringStatesNetwork;
import com.aflyingcar.warring_states.client.gui.parts.ListGui;
import com.aflyingcar.warring_states.network.messages.CitizenApplicationResultMessage;
import com.aflyingcar.warring_states.util.GuiUtils;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ListCurrentCitizenApplicationsGui extends GuiScreen {
    private final GuiScreen parent;
    private final UUID stateID;
    private final EntityPlayer player;
    private final Map<UUID, String> applications;
    private final Map<Integer, UUID> buttonIDMap;
    private ListGui<String> applicationsGui;

    public ListCurrentCitizenApplicationsGui(GuiScreen parent, UUID stateID, EntityPlayer player, Map<UUID, String> applications) {
        this.parent = parent;
        this.stateID = stateID;
        this.player = player;
        this.applications = applications;

        buttonIDMap = new HashMap<>();
    }

    protected GuiButton getAcceptButton(int index) {
        return buttonList.get(index);
    }

    protected GuiButton getRejectButton(int index) {
        return buttonList.get(index + 1);
    }

    @Override
    public void initGui() {
        buttonList.clear();

        int j = 0;
        for(UUID uuid : applications.keySet()) {
            addButton(new GuiButton(j, 0, 0, GuiUtils.getTranslatedStringWidth(fontRenderer, "accept"), 20, GuiUtils.translate("accept")));
            buttonIDMap.put(j, uuid);
            ++j;
            addButton(new GuiButton(j, 0, 0, GuiUtils.getTranslatedStringWidth(fontRenderer, "reject"), 20, GuiUtils.translate("reject")));
            buttonIDMap.put(j, uuid);
            ++j;
        }

        // TODO: Should this GUI actually be used for modifying individual citizen privilege levels?
        applicationsGui = new ListGui<String>(fontRenderer, this, mc, width, height, Lists.newArrayList(applications.values().iterator()), null, (element, i) -> element, null)
        {
            @Override
            protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {
                GuiButton acceptButton = getAcceptButton(slotIndex);
                GuiButton rejectButton = getRejectButton(slotIndex);

                String name = getElement(slotIndex);

                acceptButton.x = xPos + GuiUtils.getStringWidth(fontRenderer, name) + 20;
                rejectButton.x = acceptButton.x + acceptButton.width + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE;

                acceptButton.y = yPos;
                rejectButton.y = yPos;

                acceptButton.drawButton(mc, mouseXIn, mouseYIn, partialTicks);
                rejectButton.drawButton(mc, mouseXIn, mouseYIn, partialTicks);

                super.drawSlot(slotIndex, xPos, yPos, heightIn, mouseXIn, mouseYIn, partialTicks);
            }
        };

        applicationsGui.registerScrollButtons(j + 1, j + 2);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.enableBlend();
        drawDefaultBackground();

        // TODO: draw GUI background

        applicationsGui.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.disableBlend();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        applicationsGui.actionPerformed(button);

        // First make sure this wasn't one of the scroll buttons used by applicationsGui
        if(button.id >= 0 && button.id < buttonList.size()) {
            UUID uuid = buttonIDMap.get(button.id);
            if(uuid != null) {
                if(button.id % 2 == 0) {
                    // This is an acceptance button
                    WarringStatesNetwork.NETWORK.sendToServer(new CitizenApplicationResultMessage(stateID, player.getPersistentID(), uuid, CitizenApplicationResultMessage.Result.ACCEPT));
                } else {
                    // This is a rejection button
                    WarringStatesNetwork.NETWORK.sendToServer(new CitizenApplicationResultMessage(stateID, player.getPersistentID(), uuid, CitizenApplicationResultMessage.Result.REJECT));
                }
                buttonList.remove(button.id);
                buttonIDMap.remove(button.id);
                applicationsGui.getList().remove(applications.get(uuid));
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        applicationsGui.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
        }
    }
}
