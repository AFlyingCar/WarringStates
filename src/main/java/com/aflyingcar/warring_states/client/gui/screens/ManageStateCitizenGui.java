package com.aflyingcar.warring_states.client.gui.screens;

import com.aflyingcar.warring_states.WarringStatesNetwork;
import com.aflyingcar.warring_states.api.CitizenPrivileges;
import com.aflyingcar.warring_states.network.messages.KickPlayerFromStateMessage;
import com.aflyingcar.warring_states.network.messages.UpdatePlayerPrivilegesMessage;
import com.aflyingcar.warring_states.states.DummyCitizen;
import com.aflyingcar.warring_states.util.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.UUID;

public class ManageStateCitizenGui extends GuiScreen {
    private final GuiScreen parent;
    private final EntityPlayer player;
    private final UUID stateID;
    private final DummyCitizen citizen;
    private int posX;
    private int posY;
    private int privileges;

    private GuiButton kickButton;
    private GuiButton gobackButton;

    // TODO: Button for each possible privilege
    // TODO: Button with option of kicking player

    public ManageStateCitizenGui(GuiScreen parent, EntityPlayer player, UUID stateID, DummyCitizen citizen) {
        this.parent = parent;
        this.player = player;
        this.stateID = stateID;
        this.citizen = citizen;

        this.privileges = citizen.getPrivileges();
    }

    @Override
    public void initGui() {
        posX = (width / 4);
        posY = (height / 4);

        int i = 0;
        for(int privilege : CitizenPrivileges.values()) {
            GuiCheckBox checkBox = addButton(new GuiCheckBox(i, posX, posY, CitizenPrivileges.toString(privilege), (citizen.getPrivileges() & privilege) == privilege) {
                @Override
                public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                    boolean result = super.mousePressed(mc, mouseX, mouseY);

                    int privilegeValue = CitizenPrivileges.values()[id];

                    if(isChecked()) {
                        privileges = privileges | privilegeValue;
                    } else {
                        privileges = privileges & ~privilegeValue;
                    }

                    return result;
                }
            });
            posX += checkBox.width + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE;
            ++i;
            if(i % 3 == 0) {
                posY += checkBox.height + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE;
                posX = width / 4;
            }
        }

        posX = (width / 2);
        posY += buttonList.stream().findAny().map(b -> b.height + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE).orElse(0);

        kickButton = addButton(new GuiButton(i, posX, posY, GuiUtils.getTranslatedStringWidth(fontRenderer, "kick"), 20, GuiUtils.translate("kick")));
        posY += kickButton.height + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE;
        ++i;

        gobackButton = addButton(new GuiButton(i, posX, posY, GuiUtils.getTranslatedStringWidth(fontRenderer, "goback"), 20, GuiUtils.translate("goback")));
        kickButton.x += gobackButton.width / 4;
        ++i;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawString(fontRenderer, GuiUtils.translate("citizen_name", citizen.getName()), width / 2 - GuiUtils.getTranslatedStringWidth(fontRenderer, "citizen_name", citizen.getName()) / 4, (height / 4) - 20, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if(button.id >= CitizenPrivileges.values().length) {
            if(button.id == kickButton.id) {
                WarringStatesNetwork.NETWORK.sendToServer(new KickPlayerFromStateMessage(stateID, player, citizen));
                ((ListCitizenManagementGui)parent).removeCitizen(citizen);
            } else if(button.id == gobackButton.id) {
                if(isDirty()) {
                    WarringStatesNetwork.NETWORK.sendToServer(new UpdatePlayerPrivilegesMessage(stateID, player, new DummyCitizen(citizen.getCitizenID(), privileges, citizen.getName())));
                }
            }
            mc.displayGuiScreen(parent);
        }
    }

    private boolean isDirty() {
        return privileges != citizen.getPrivileges();
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
