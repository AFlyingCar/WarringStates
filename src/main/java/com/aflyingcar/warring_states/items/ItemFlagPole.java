package com.aflyingcar.warring_states.items;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CreativeTab;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.item.Item;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemFlagPole extends Item {
    public static final String TRANSLATION_KEY = "flag_pole";

    public ItemFlagPole() {
        setTranslationKey(TRANSLATION_KEY);
        setMaxDamage(0);
        setCreativeTab(CreativeTab.TAB);
        setRegistryName(WarringStatesMod.MOD_ID, TRANSLATION_KEY);
    }
}
