package com.aflyingcar.warring_states.client.gui.screens;

import com.aflyingcar.warring_states.util.GuiUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Consumer;

public class EditStringGui extends GuiScreen {
    private final GuiScreen parent;
    private final Consumer<String> onEditConfirm;
    private final String instructionsTranslationKey;
    private final String defaultValue;

    private GuiTextField field;

    private final EntityPlayer player;

    public EditStringGui(@Nullable GuiScreen parent, @Nonnull EntityPlayer player, @Nullable Consumer<String> onEditConfirm, @Nonnull String key, @Nullable String defaultValue) {
        this.parent = parent;
        this.player = player;
        this.onEditConfirm = onEditConfirm;
        this.instructionsTranslationKey = key;
        this.defaultValue = defaultValue;
    }

    @Override
    public void initGui() {
        int posX = width / 4;
        int posY = 100;

        // X == width - a quarter of the width
        field = new GuiTextField(0, fontRenderer, posX, posY, width / 2, 20);
        field.setFocused(true);
        field.setCanLoseFocus(false);
        if(defaultValue != null) field.setText(defaultValue);
        posY += 40;

        posX += width / 4;
        GuiButton confirm = addButton(new GuiButton(0, posX, posY, GuiUtils.getTranslatedStringWidth(fontRenderer, "confirm"), 20, GuiUtils.translate("confirm")));
        GuiButton cancel = addButton(new GuiButton(1, posX + confirm.width + GuiUtils.DEFAULT_TEXT_BUFFER_SIZE, posY, GuiUtils.getTranslatedStringWidth(fontRenderer, "cancel"), 20, GuiUtils.translate("cancel")));
    }

    @Override
    public void updateScreen() {
        field.updateCursorCounter();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, GuiUtils.translate(instructionsTranslationKey), width / 2, 70, 16777215);

        field.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        switch(keyCode) {
            case Keyboard.KEY_ESCAPE:
                cancel();
                break;
            case Keyboard.KEY_RETURN:
                confirm();
                break;
            default:
                field.textboxKeyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch(button.id) {
            case 0:
                confirm();
            case 1:
            default:
                cancel();
                break;
        }
    }

    protected void cancel() {
        if(parent != null) {
            mc.displayGuiScreen(parent);
        } else {
            player.closeScreen();
        }
    }

    protected void confirm() {
        if(onEditConfirm != null) {
            onEditConfirm.accept(field.getText());
        }
    }
}
