package com.aflyingcar.warring_states.client.gui.screens;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class ConfirmDeclareWarGui extends ConfirmActionGui {
    public final static String CONFIRMATION_QUESTION_KEY = "";

    public ConfirmDeclareWarGui(@Nullable GuiScreen parentGui, @Nonnull EntityPlayer player, @Nullable Runnable onConfirm, @Nullable Runnable onCancel, String targetName) {
        super(parentGui, player, onConfirm, onCancel, CONFIRMATION_QUESTION_KEY, targetName);
    }

    public ConfirmDeclareWarGui(@Nullable GuiScreen parentGui, @Nonnull EntityPlayer player, @Nullable Runnable onConfirm, @Nullable Runnable onCancel, boolean closeOnClick, String targetName) {
        super(parentGui, player, onConfirm, onCancel, closeOnClick, CONFIRMATION_QUESTION_KEY, targetName);
    }

    @Override
    public void initGui() {
        mc.displayGuiScreen(new GuiYesNo(this, getMessage(), "", 0) {
            @Override
            public void initGui() {
                super.initGui();

                // TODO: Shift these buttons down
                buttonList.get(0).y += 0;
                buttonList.get(1).y += 0;
            }

            @Override
            public void drawScreen(int mouseX, int mouseY, float partialTicks) {
                drawDefaultBackground();

                // TODO: Shift this up
                drawCenteredString(fontRenderer, getMessage(), width / 2, 70, 16777215);

                int j;
                for(j = 0; j < this.buttonList.size(); ++j) {
                    this.buttonList.get(j).drawButton(this.mc, mouseX, mouseY, partialTicks);
                }
            }
        });
    }
}
