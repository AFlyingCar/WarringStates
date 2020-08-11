package com.aflyingcar.warring_states.client.gui.screens;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class ConfirmActionGui extends GuiScreen {
    private final GuiScreen parent;
    private final EntityPlayer player;
    private final Runnable onConfirm;
    private final Runnable onCancel;
    private final String message;
    private final boolean closeOnClick;

    public ConfirmActionGui(@Nullable GuiScreen parentGui, @Nonnull EntityPlayer player, @Nullable Runnable onConfirm, @Nullable Runnable onCancel, @Nonnull String message_translation_key, @Nonnull Object... args) {
        this(parentGui, player, onConfirm, onCancel, false, message_translation_key, args);
    }

    public ConfirmActionGui(@Nullable GuiScreen parentGui, @Nonnull EntityPlayer player, @Nullable Runnable onConfirm, @Nullable Runnable onCancel, boolean closeOnClick, @Nonnull String message_translation_key, @Nonnull Object... args) {
        this.parent = parentGui;
        this.player = player;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        this.message = I18n.format(message_translation_key, args);
        this.closeOnClick = closeOnClick;
    }

    @Override
    public void initGui() {
        mc.displayGuiScreen(new GuiYesNo(this, message, "", 0));
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if(result) {
            if(onConfirm != null) onConfirm.run();
        } else {
            if(onCancel != null) onCancel.run();
        }

        if(closeOnClick) {
            player.closeScreen();
        } else {
            mc.displayGuiScreen(parent);
        }
    }
}
