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

    private final int textureWidth = 150;
    private final int textureHeight = 150;

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

        posX = (this.width - textureWidth) / 2;
        posY = (this.height - textureHeight) / 2;

        buttonList.add(new GuiButton(0, posX + textureWidth, posY + textureHeight, GuiUtils.getStringWidth(fontRenderer, "Cancel"), 20, "Cancel"));
        buttonList.add(new GuiButton(1, posX, posY + textureHeight, GuiUtils.getStringWidth(fontRenderer, "Confirm"), 20, "Confirm"));

        nameField = new GuiTextField(2, fontRenderer, posX, posY, textureWidth, 20);
        nameField.setFocused(true);
        nameField.setCanLoseFocus(true);

        descField = new GuiTextField(3, fontRenderer, posX, posY + 20, textureWidth, 20);
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

        // TODO
        // this.mc.renderEngine.bindTexture(new ResourceLocation(WarringStatesMod.MOD_ID, "wtf???"));

        this.posX = (this.width - textureWidth) / 2;
        this.posY = (this.height - textureHeight) / 2;

        // TODO: Draw GUI window
        drawTexturedModalRect(posX, posY, 0, 0, textureWidth, textureHeight);

        drawString(fontRenderer, "State Creation Wizard", posX + 20, posY + 31, Color.white.getRGB());

        // TODO: We should draw labels for what these fields mean
        nameField.drawTextBox();
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
