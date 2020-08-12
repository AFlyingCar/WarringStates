package com.aflyingcar.warring_states.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class RestorableBlock implements ISerializable{
    private ExtendedBlockPos pos;
    private IBlockState originalState;
    private TileEntity tileEntity;

    private IBlockState placedBlock;
    private boolean wasPreviouslyReplacement;
    private boolean isCurrentlyRestoring;

    public RestorableBlock() { }

    public RestorableBlock(@Nonnull ExtendedBlockPos pos, @Nonnull IBlockState originalState, @Nullable TileEntity tileEntity) {
        this.pos = pos;
        this.originalState = originalState;
        this.tileEntity = tileEntity;
        this.wasPreviouslyReplacement = false;
    }

    public RestorableBlock(@Nonnull ExtendedBlockPos pos, @Nonnull IBlockState originalState, @Nullable TileEntity tileEntity, @Nonnull IBlockState placedBlock) {
        this(pos, originalState, tileEntity);

        this.placedBlock = placedBlock;
        this.wasPreviouslyReplacement = true;
    }

    @Nonnull
    public ExtendedBlockPos getPos() {
        return pos;
    }

    @Nonnull
    public IBlockState getOriginalState() {
        return originalState;
    }

    @Nullable
    public IBlockState getPlacedBlock() {
        return placedBlock;
    }

    @Nullable
    public TileEntity getOriginalTileEntity() {
        return tileEntity;
    }

    public boolean isReplacementOperation() {
        // We are replacing a block if there is a block that is getting placed, and if the block we are placing is not air
        return placedBlock != null && !(placedBlock instanceof BlockAir);
    }

    public boolean wasPreviouslyReplacement() {
        return this.wasPreviouslyReplacement;
    }

    @Override
    public NBTTagCompound writeNBT(NBTTagCompound nbt) {
        nbt.setInteger("x", pos.getX());
        nbt.setInteger("y", pos.getY());
        nbt.setInteger("z", pos.getZ());
        nbt.setInteger("dimID", pos.getDimID());
        nbt.setInteger("originalState", Block.getStateId(originalState));
        nbt.setBoolean("wasPreviouslyReplacement", wasPreviouslyReplacement);

        if(tileEntity != null) {
            NBTTagCompound nbtTileEntity = new NBTTagCompound();
            tileEntity.writeToNBT(nbtTileEntity);
            nbt.setTag("tileEntity", nbtTileEntity);
        }

        if(placedBlock != null) {
            nbt.setInteger("placedBlock", Block.getStateId(placedBlock));
        }

        return nbt;
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        pos = new ExtendedBlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"), nbt.getInteger("dimID"));
        originalState = Block.getStateById(nbt.getInteger("originalState"));

        if(nbt.hasKey("wasPreviouslyReplacement")) {
            wasPreviouslyReplacement = nbt.getBoolean("wasPreviouslyReplacement");
        }

        if(nbt.hasKey("tileEntity")) {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(pos.getDimID());

            tileEntity = TileEntity.create(world, nbt.getCompoundTag("tileEntity"));
        }

        if(nbt.hasKey("placedBlock")) {
            placedBlock = Block.getStateById(nbt.getInteger("placedBlock"));
        }
    }

    public void restore() {
        isCurrentlyRestoring = true;
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(pos.getDimID());

        // Flags == 2 means notify the client of a block update
        world.setBlockState(pos, originalState, 11);
        // WorldUtils.notifySurroundingBlocksOfStateChange(world, pos, originalState);

        if(placedBlock != null) {
            placedBlock.getBlock().dropBlockAsItem(world, pos, placedBlock, 0);
        }
        isCurrentlyRestoring = false;
    }

    public boolean isCurrentlyRestoring() {
        return isCurrentlyRestoring;
    }

    public void setPlacedBlock(IBlockState placedBlock) {
        this.wasPreviouslyReplacement = isReplacementOperation();
        this.placedBlock = placedBlock;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        RestorableBlock that = (RestorableBlock) o;
        return Objects.equals(pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }
}
