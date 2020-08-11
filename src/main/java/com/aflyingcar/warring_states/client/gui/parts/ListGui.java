package com.aflyingcar.warring_states.client.gui.parts;

import com.aflyingcar.warring_states.util.QuadConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ListGui<T> extends GuiSlot {
    private final List<T> list;
    private final GuiScreen parent;
    private final FontRenderer fontRenderer;
    private final QuadConsumer<T, Boolean, Integer, Integer> onElementClicked;
    private final BiFunction<T, Integer, String> getStringRepresentationOfElement;
    private final Function<T, String> getTooltipText;

    public ListGui(@Nonnull FontRenderer fontRenderer, @Nonnull GuiScreen parent, @Nonnull Minecraft mc, int width, int height, @Nonnull List<T> list, @Nullable QuadConsumer<T, Boolean, Integer, Integer> onElementClicked, @Nullable BiFunction<T, Integer, String> getStringRepresentationOfElement, @Nullable Function<T, String> getTooltipText) {
        // TODO: top and bottom???
        super(mc, width, height, 32, height - 65 + 4, 20);
        this.list = list;
        this.parent = parent;
        this.fontRenderer = fontRenderer;
        this.onElementClicked = onElementClicked;
        this.getStringRepresentationOfElement = getStringRepresentationOfElement;
        this.getTooltipText = getTooltipText;
    }

    @Override
    protected int getSize() {
        return list.size();
    }

    protected T getElement(int i) {
        return list.get(i);
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        if(onElementClicked != null)
            onElementClicked.accept(list.get(slotIndex), isDoubleClick, mouseX, mouseY);
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return false;
    }

    @Override
    protected void drawBackground() {
    }

    @Override
    protected void drawContainerBackground(Tessellator tessellator) {
    }

    @Override
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
    }

    @Override
    protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {
        // Add 20 to yPos before checking bottom to account for the height of the text
        if(yPos < top || (yPos + 20) > bottom) return;

        String string = getStringRepresentationOfElement == null ? list.get(slotIndex).toString() : getStringRepresentationOfElement.apply(list.get(slotIndex), slotIndex);
        parent.drawString(fontRenderer, string, xPos, yPos, 0xFFFFFF);

        if(getTooltipText != null) {
            // TODO: Should we draw a tooltip if the mouse is over this slot??
            //  We can check by seeing if mouseXIn, mouseYIn is within the area covered by the conflict entry
        }
    }

    public List<T> getList() {
        return list;
    }
}
