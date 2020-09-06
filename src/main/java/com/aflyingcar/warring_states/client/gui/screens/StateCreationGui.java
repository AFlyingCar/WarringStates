package com.aflyingcar.warring_states.client.gui.screens;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.WarringStatesNetwork;
import com.aflyingcar.warring_states.network.messages.CreateStateMessage;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import com.aflyingcar.warring_states.util.ExtendedBlockPos;
import com.aflyingcar.warring_states.util.GuiUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class StateCreationGui extends GuiScreen {
    private final TileEntityClaimer claimer;
    private final EntityPlayer player;

    private final int textureWidth = 170;
    private final int textureHeight = 150;

    private final int LBORDER_WIDTH = 8;
    private final int RBORDER_WIDTH = 7;
    private final int TBORDER_WIDTH = 8;
    private final int BBORDER_WIDTH = 6;

    private int posX = 0;
    private int posY = 0;

    private GuiTextField nameField;
    private GuiTextField descField;

    public StateCreationGui(TileEntityClaimer claimer, EntityPlayer player) {
        this.claimer = claimer;
        this.player = player;
    }

    public void initGui() {
        buttonList.clear();

        posX = (this.width - textureWidth) / 2 + LBORDER_WIDTH;
        posY = (this.height - textureHeight) / 2 + TBORDER_WIDTH;

        int cancelWidth = GuiUtils.getTranslatedStringWidth(fontRenderer, "cancel");
        int confirmWidth = GuiUtils.getTranslatedStringWidth(fontRenderer, "confirm");

        int buttonYPos = posY + textureHeight - BBORDER_WIDTH - TBORDER_WIDTH - 20 - 5;

        buttonList.add(new GuiButton(0, posX + textureWidth - cancelWidth - RBORDER_WIDTH - LBORDER_WIDTH, buttonYPos, cancelWidth, 20, GuiUtils.translate("cancel")));
        buttonList.add(new GuiButton(1, posX, buttonYPos, confirmWidth, 20, GuiUtils.translate("confirm")));

        int titleHeight = 20;

        int textFieldWidth = textureWidth - (LBORDER_WIDTH + RBORDER_WIDTH);

        int labelHeight = 20;
        posY += titleHeight + labelHeight;

        nameField = new GuiTextField(2, fontRenderer, posX, posY, textFieldWidth, 20);
        nameField.setFocused(true);
        nameField.setCanLoseFocus(true);

        posY += labelHeight + nameField.height;
        descField = new GuiTextField(3, fontRenderer, posX, posY, textFieldWidth, 20);
        descField.setCanLoseFocus(true);
    }

    @Override
    public void updateScreen() {
        nameField.updateCursorCounter();
        descField.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        switch(keyCode) {
            case Keyboard.KEY_ESCAPE:
                cancelCreation();
                break;
            case Keyboard.KEY_RETURN:
                confirmCreation();
                break;
            default:
                if(nameField.isFocused())
                    nameField.textboxKeyTyped(typedChar, keyCode);
                else if(descField.isFocused())
                    descField.textboxKeyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draws slightly darkened background
        drawDefaultBackground();

        this.mc.renderEngine.bindTexture(new ResourceLocation(WarringStatesMod.MOD_ID, "textures/gui/state_creation.png"));

        this.posX = (this.width - textureWidth) / 2;
        this.posY = (this.height - textureHeight) / 2;

        drawTexturedModalRect(posX, posY, 0, 0, textureWidth, textureHeight);

        posX += LBORDER_WIDTH;
        posY += TBORDER_WIDTH;

        int titleWidth = GuiUtils.getTranslatedStringWidth(fontRenderer, "state_creator_title");
        drawString(fontRenderer, GuiUtils.translate("state_creator_title"), posX + titleWidth / 2, posY, Color.white.getRGB());
        posY += 20 + 5;

        drawString(fontRenderer, GuiUtils.translate("state_creator_name"), nameField.x, posY + 4, Color.white.getRGB());
        nameField.drawTextBox();
        posY += 20 + nameField.height + 5;

        drawString(fontRenderer, GuiUtils.translate("state_creator_desc"), descField.x, posY, Color.white.getRGB());
        descField.drawTextBox();

        // Draw buttons and stuff
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch(button.id) {
            case 0:
                cancelCreation();
                break;
            case 1:
                confirmCreation();
                break;
        }
    }

    private void confirmCreation() {
        if(!nameField.getText().isEmpty()) {
            UUID stateID = UUID.randomUUID();

            claimer.changeOwner(nameField.getText(), descField.getText(), stateID);

            WarringStatesNetwork.NETWORK.sendToServer(new CreateStateMessage(new ExtendedBlockPos(claimer.getPos(), player.dimension), nameField.getText(), descField.getText(), stateID, player.getPersistentID()));

            player.closeScreen();
        }
    }

    private void cancelCreation() {
        WarringStatesMod.getLogger().info("Cancelling the creation of a state @" + claimer.getPos());
        WarringStatesNetwork.NETWORK.sendToServer(new CreateStateMessage(new ExtendedBlockPos(claimer.getPos(), player.dimension)));

        player.closeScreen();
    }
}
