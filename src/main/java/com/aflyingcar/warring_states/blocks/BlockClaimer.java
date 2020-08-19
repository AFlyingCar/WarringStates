package com.aflyingcar.warring_states.blocks;

import com.aflyingcar.warring_states.WarringStatesMod;
import com.aflyingcar.warring_states.api.CreativeTab;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockClaimer extends Block implements ITileEntityProvider {
    public static final String NAME = "claimer";

    public static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, 2, 1);
    public static final IProperty<ClaimerHalf> HALF = PropertyEnum.create("half", ClaimerHalf.class);

    public enum ClaimerHalf implements IStringSerializable {
        TOP("top"),
        BOTTOM("bottom");

        private String name;

        ClaimerHalf(String name) {
            this.name = name;
        }

        @Nullable
        public static ClaimerHalf fromInt(int meta) {
            return meta < 0 || meta >= values().length ? null : values()[meta];
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public BlockClaimer() {
        super(Material.CLOTH);

        setTranslationKey(WarringStatesMod.MOD_ID + "." + NAME);
        setRegistryName(WarringStatesMod.MOD_ID, NAME);
        setHardness(1.0f);
        setCreativeTab(CreativeTab.TAB);
        setResistance(600000000000000.0f); // Make this impossibly large (like bedrock)
        setBlockUnbreakable(); // Honestly, just make it unbreakable
        setDefaultState(getBlockState().getBaseState().withProperty(HALF, ClaimerHalf.BOTTOM));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HALF);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(HALF, Objects.requireNonNull(ClaimerHalf.fromInt(meta)));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HALF).ordinal();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        // return BOUNDING_BOX;
        return FULL_BLOCK_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB; // TODO: Should we have a custom bounding box?
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && worldIn.isAirBlock(pos.up());
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        // Just refer to the bottom one if we are the top
        if(state.getValue(HALF) == ClaimerHalf.TOP) {
            pos = pos.down();
        }

        TileEntityClaimer te = (TileEntityClaimer) worldIn.getTileEntity(pos);
        if(te != null) {
            return te.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
        }

        return true;
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return state.getValue(HALF) == ClaimerHalf.BOTTOM;
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityClaimer();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos, getDefaultState().withProperty(HALF, ClaimerHalf.BOTTOM));
        worldIn.setBlockState(pos.up(), getDefaultState().withProperty(HALF, ClaimerHalf.TOP));
    }

    // TODO: This shouldn't actually be harvestable _at all_ except by creative OPs
    //  Instead, players should be forced to go through the GUI to dismantle it
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if(state.getValue(HALF) == ClaimerHalf.TOP) {
            pos = pos.down();
        }

        TileEntityClaimer te = (TileEntityClaimer) worldIn.getTileEntity(pos);

        // This can only be harvested by players who are in the state
        if(te != null) {
            te.onBlockHarvested(worldIn, pos, state, player);
            super.onBlockHarvested(worldIn, pos, state, player);
        }
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }
}
