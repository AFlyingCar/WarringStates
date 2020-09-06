package com.aflyingcar.warring_states.api;

import com.aflyingcar.warring_states.WarringStatesItems;
import com.aflyingcar.warring_states.WarringStatesMod;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

@MethodsReturnNonnullByDefault
public class CreativeTab extends CreativeTabs {
    public static final CreativeTab TAB = new CreativeTab();

    public CreativeTab() {
        super(WarringStatesMod.MOD_ID);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(WarringStatesItems.CLAIMER);
    }
}
