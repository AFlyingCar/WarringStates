package com.aflyingcar.warring_states.items;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CreativeTab;
import com.aflyingcar.warring_states.api.WarringStatesAPI;
import com.aflyingcar.warring_states.states.State;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.util.WorldUtils;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemWargoalClaimer extends Item {
    public static final String TRANSLATION_KEY = "wargoal_claimer";

    public ItemWargoalClaimer() {
        setTranslationKey(TRANSLATION_KEY);
        setMaxDamage(0);
        setMaxStackSize(1);
        setCreativeTab(CreativeTab.TAB);
        setRegistryName(WarringStatesMod.MOD_ID, TRANSLATION_KEY);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        // Only do this action on the server
        if(!worldIn.isRemote) {
            State playerState = StateManager.getInstance().getStateFromPlayer(playerIn);
            if(playerState == null) {
                playerIn.sendMessage(new TextComponentTranslation("warring_states.messages.cannot_steal_for_nobody"));
                return new ActionResult<>(EnumActionResult.FAIL, playerIn.getHeldItem(handIn));
            }

            State owningState = StateManager.getInstance().getStateAtPosition(worldIn, playerIn.getPosition());
            if(owningState == null) {
                playerIn.sendMessage(new TextComponentTranslation("warring_states.messages.cannot_steal_from_nobody"));
                return new ActionResult<>(EnumActionResult.FAIL, playerIn.getHeldItem(handIn));
            }

            ChunkPos chunkPos = worldIn.getChunk(playerIn.getPosition()).getPos();
            WarringStatesAPI.claimStealChunkWargoal(playerIn, playerState, owningState, chunkPos, WorldUtils.getDimensionIDForWorld((WorldServer)worldIn));
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
