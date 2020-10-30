package com.aflyingcar.warring_states.potion;

import com.aflyingcar.warring_states.WarringStatesMod;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;

import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
public class PotionSlowFalling extends Potion {
    public PotionSlowFalling() {
        super(false /* isBadEffect */, 0xFFFFFF /* liquidColor */);

        setPotionName("effect.warring_states.slow_falling");
        setRegistryName(WarringStatesMod.MOD_ID, getName());
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if(entity.world.isRemote || (entity instanceof EntityPlayer && ((EntityPlayer)entity).isSpectator())) return;

        if(entity.motionY > 0.5 && !entity.onGround) {
            entity.motionY -= 0.07;
        }

        // If we are on the ground, remove this effect immediately
        if(entity.onGround) {
            entity.removePotionEffect(this);
        }
    }

    @Override
    public boolean isInstant() {
        return false;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>();
    }

    @Override
    public boolean isReady(int p_isReady_1_, int p_isReady_2_) {
        return true;
    }
}
