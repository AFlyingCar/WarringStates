package com.aflyingcar.warring_states.items;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CreativeTab;
import com.aflyingcar.warring_states.api.WarringStatesAPI;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemWargoalClaimer extends Item {
    public static final String TRANSLATION_KEY = "wargoal_claimer";

    public ItemWargoalClaimer() {
        setTranslationKey(TRANSLATION_KEY);
        setMaxDamage(Integer.MAX_VALUE);
        setMaxStackSize(1);
        setCreativeTab(CreativeTab.TAB);
        setRegistryName(WarringStatesMod.MOD_ID, TRANSLATION_KEY);
    }

    private boolean claimWargoal(int goalType, EntityPlayer player, State playerState, World world) {
        State owningState = StateManager.getInstance().getStateAtPosition(world, player.getPosition());
        if(owningState == null) {
            return false;
        }

        return WarringStatesAPI.claimWargoal(goalType, player, playerState, owningState, world);
    }

    private int getAdjustedWargoalType(int goalType) {
        int registeredTypes = WarringStatesAPI.getRegisteredWargoals().size();
        return registeredTypes > 0 ? (goalType % registeredTypes) : 0;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        // Only do this action on the server
        if(!worldIn.isRemote) {
            // TODO: We should honestly probably use NBT data rather than item damage data to hold this information
            //  We should also probably have a GUI for changing the wargoal type rather than simply cycling it

            ItemStack wargoalClaimerStack = playerIn.getHeldItem(handIn);
            int goalType = wargoalClaimerStack.getItemDamage();

            // If we are sneaking, then change the type of wargoal we are trying to claim
            if(playerIn.isSneaking()) {
                // Modulus so that it wraps back around
                wargoalClaimerStack.setItemDamage(getAdjustedWargoalType(goalType + 1));
                return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
            }

            State playerState = StateManager.getInstance().getStateFromPlayer(playerIn);

            if(playerState == null) {
                playerIn.sendMessage(new TextComponentTranslation("warring_states.messages.cannot_steal_for_nobody"));
                return new ActionResult<>(EnumActionResult.FAIL, playerIn.getHeldItem(handIn));
            }

            // Actually attempt to claim the wargoal now
            // Modulus it again (just to be safe) and add 1 to it to shift it up and make it 1 indexed rather than 0 indexed
            if(!claimWargoal(getAdjustedWargoalType(goalType) + 1, playerIn, playerState, worldIn)) {
                playerIn.sendMessage(new TextComponentTranslation("warring_states.messages.cannot_steal_from_nobody"));
                return new ActionResult<>(EnumActionResult.FAIL, playerIn.getHeldItem(handIn));
            } else {
                return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
            }
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
