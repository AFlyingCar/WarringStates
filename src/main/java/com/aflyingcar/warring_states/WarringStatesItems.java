package com.aflyingcar.warring_states;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(WarringStatesMod.MOD_ID)
public class WarringStatesItems {
    public final static Item ITEM_CLAIMER_BLOCK;
    public final static Item ITEM_WARGOAL_CLAIMER;

    static {
        ITEM_CLAIMER_BLOCK = null;
        ITEM_WARGOAL_CLAIMER = null;
    }
}
