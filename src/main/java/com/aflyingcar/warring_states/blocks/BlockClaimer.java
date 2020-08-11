package com.aflyingcar.warring_states.blocks;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CreativeTab;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BlockClaimer extends Block implements ITileEntityProvider {
    public static final String NAME = "claimer";

    public BlockClaimer() {
        super(Material.CLOTH);

        setTranslationKey(WarringStatesMod.MOD_ID + "." + NAME);
        setRegistryName(WarringStatesMod.MOD_ID, NAME);
        setHardness(1.0f);
        setCreativeTab(CreativeTab.TAB);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntityClaimer te = (TileEntityClaimer) worldIn.getTileEntity(pos);
        if(te != null) {
            return te.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
        }

        return true;
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityClaimer();
    }

    // TODO: This shouldn't actually be harvestable _at all_ except by creative OPs
    //  Instead, players should be forced to go through the GUI to dismantle it
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        TileEntityClaimer te = (TileEntityClaimer) worldIn.getTileEntity(pos);

        // This can only be harvested by players who are in the state
        if(te != null && te.canBeHarvested(player)) {
            te.onBlockHarvested(worldIn, pos, state, player);
            super.onBlockHarvested(worldIn, pos, state, player);
        }
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }
}
