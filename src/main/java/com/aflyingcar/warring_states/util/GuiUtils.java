package com.aflyingcar.warring_states.util;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.client.gui.GuiID;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class GuiUtils {
    /**
     * A buffer of space to place around text to prevent text from butting up against each other
     */
    public static final int DEFAULT_TEXT_BUFFER_SIZE = 5;

    /**
     * A cache of calculated string widths, so that it doesn't have to be calculated multiple times
     */
    private static final Map<String, Integer> stringWidthMap = new HashMap<>();

    /**
     * Helper function to make opening GUIs a little easier
     * @param player The player to open a GUI for
     * @param pos The position to open the GUI at
     * @param guiID The ID of the GUI to open
     */
    public static void openGUI(EntityPlayer player, BlockPos pos, GuiID guiID) {
        player.openGui(WarringStatesMod.INSTANCE, guiID.ordinal(), player.world, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Gets the width, in pixels, of the {@code String} if rendered with the given {@code FontRenderer}
     * @param fontRenderer The {@code FontRenderer} to use to determine the text's width
     * @param text The text to determine the width of
     * @return The width of {@code text} as rendered by {@code fontRenderer}, with {@code DEFAULT_TEXT_BUFFER_SIZE} added in
     */
    public static int getStringWidth(FontRenderer fontRenderer, String text) {
        return getStringWidth(fontRenderer, text, true);
    }

    /**
     * Gets the width, in pixels, of the {@code String} if rendered with the given {@code FontRenderer}
     * @param fontRenderer The {@code FontRenderer} to use to determine the text's width
     * @param text The text to determine the width of
     * @param addBuffer Whether or not {@code DEFAULT_TEXT_BUFFER_SIZE} should be added to the final calculation (note, this is not stored in the cache)
     * @return The width of {@code text} as rendered by {@code fontRenderer}, with {@code DEFAULT_TEXT_BUFFER_SIZE} added in if {@code addBuffer} is {@code true}
     */
    public static int getStringWidth(FontRenderer fontRenderer, String text, boolean addBuffer) {
        if(stringWidthMap.containsKey(text)) {
            return stringWidthMap.get(text) + (addBuffer ? DEFAULT_TEXT_BUFFER_SIZE : 0);
        } else {
            int width = fontRenderer.getStringWidth(text);
            stringWidthMap.put(text, width);
            return width + (addBuffer ? DEFAULT_TEXT_BUFFER_SIZE : 0);
        }
    }

    public static int getTranslatedStringWidth(FontRenderer fontRenderer, String textKey, Object... args) {
        return getTranslatedStringWidth(fontRenderer, true, textKey, args);
    }

    public static int getTranslatedStringWidth(FontRenderer fontRenderer, boolean addBuffer, String textKey, Object... args) {
        return getStringWidth(fontRenderer, translate(textKey, args), addBuffer);
    }

    public static String translate(String translationKey, Object... args) {
        return I18n.format("warring_states.gui." + translationKey, args);
    }

    public static int getPositionToCenterElementOnTexture(int screenWidth, int textureWidth, int elementWidth) {
        return ((screenWidth - textureWidth) / 2) + (textureWidth / 2) - elementWidth / 2;
    }

    public static int getPositionToCenterTextOnTexture(int screenWidth, int textureWidth, FontRenderer fontRenderer, String key, String... args) {
        return getPositionToCenterElementOnTexture(screenWidth, textureWidth, getTranslatedStringWidth(fontRenderer, key, args));
    }

    public static int getPositionToCenterStringOnTexture(int screenWidth, int textureWidth, FontRenderer fontRenderer, String text) {
        return getPositionToCenterElementOnTexture(screenWidth, textureWidth, getStringWidth(fontRenderer, text));
    }
}
