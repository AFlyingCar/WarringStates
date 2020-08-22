package com.aflyingcar.warring_states.util;

import com.aflyingcar.warring_states.WarringStatesBlocks;
import com.aflyingcar.warring_states.blocks.BlockClaimer;
import com.aflyingcar.warring_states.states.DummyState;
import com.aflyingcar.warring_states.states.StateManager;
import com.aflyingcar.warring_states.tileentities.TileEntityClaimer;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WorldUtils {
    /**
     * Converts a {@code ChunkPos} into a {@code BlockPos}
     * @param chunkPos The {@code ChunkPos} to convert
     * @param y The Y coordinate of the new block position
     * @return A new {@code BlockPos} that corresponds to the origin of the given {@code ChunkPos}
     */
    @Nonnull
    public static BlockPos chunkPosToBlockPos(ChunkPos chunkPos, int y) {
        return new BlockPos(chunkPos.x * 16, y, chunkPos.z * 16);
    }

    /**
     * Converts a {@code ChunkPos} into a {@code BlockPos}
     * @param chunkPos The {@code ChunkPos} to convert
     * @return A new {@code BlockPos} that corresponds to the origin of the given {@code ChunkPos} at Y=0
     */
    @Nonnull
    public static BlockPos chunkPosToBlockPos(ChunkPos chunkPos) {
        return chunkPosToBlockPos(chunkPos, 0);
    }

    /**
     * Gets a list of all entities that are within the given chunk
     * @param klass The {@code Class} object of the entity type to search for
     * @param world The {@code World} to get a chunk in
     * @param pos A {@code BlockPos} that is located in the chunk to search in
     * @param filter A function to filter out some entries
     * @param <T> The type of {@code Entity} to search for
     * @return A {@code List} of all entities within the chunk of type {@code <T>} that can pass through {@code filter}
     */
    @Nonnull
    public static <T extends Entity> List<T> getEntitiesWithinChunk(Class<? extends T> klass, World world, BlockPos pos, @SuppressWarnings("Guava") @Nullable Predicate<? super T > filter) {
        Chunk c = world.getChunk(pos);

        // Left (X), Bottom (Y), Back (Z) corner
        BlockPos lbb_corner = chunkPosToBlockPos(c.getPos());

        // Right (X), Top (Y), Front (Z) corner
        BlockPos rtf_corner = chunkPosToBlockPos(new ChunkPos(c.x + 1, c.z + 1), world.getHeight());

        List<T> entities = Lists.newArrayList();
        c.getEntitiesOfTypeWithinAABB(klass, new AxisAlignedBB(lbb_corner, rtf_corner), entities, filter);

        return entities;
    }

    /**
     * Checks whether the given block position exists within the given chunk position.
     * Note that this does not do any checks for dimensionality, and assumes both the block position and chunk positions are in the same dimension
     * @param chunkPos The {@code ChunkPos} to check in
     * @param blockPos The {@code BlockPos} to check
     * @return True if the given {@code BlockPos} is within the given {@code ChunkPos}
     */
    public static boolean isBlockWithinChunk(ChunkPos chunkPos, BlockPos blockPos) {
        // Get the chunk XZ position in world space
        BlockPos chunkPosition = chunkPosToBlockPos(chunkPos);

        return blockPos.getX() > chunkPosition.getX() && blockPos.getX() < chunkPosition.getX() + 16 &&
               blockPos.getZ() > chunkPosition.getZ() && blockPos.getZ() < chunkPosition.getZ() + 16;
    }

    /**
     * Gets the basic information of the state which owns the Claimer at the given position
     * @param world The world
     * @param pos The position of the claimer
     * @return The basic information of the state that owns the claimer, or null if there is no claimer
     */
    @Nullable
    public static DummyState getStateInfoAtPosition(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);

        if(te instanceof TileEntityClaimer) {
            TileEntityClaimer tec = (TileEntityClaimer)te;
            return new DummyState(tec.getStateUUID(), tec.getStateName(), tec.getStateDesc());
        } else {
            return null;
        }
    }

    /**
     * Gets all TileEntities within the given chunk whose type matches T
     * @param klass The Class object for T
     * @param world The world to search
     * @param pos The chunk position to search within
     * @param <T> The type of TileEntity to search for
     * @return A List of all TileEntities which are instances of {@code <T>}
     */
    @Nonnull
    public static <T extends TileEntity> List<T> getTileEntitiesWithinChunk(Class<T> klass, World world, ChunkPos pos) {
        Chunk c = world.getChunk(pos.x, pos.z);

        return c.getTileEntityMap().values().stream().filter(klass::isInstance).map(klass::cast).collect(Collectors.toList());
    }

    /**
     * Gets the dimension ID of the given World
     * @param world The world
     * @return The dimension ID of the world.
     */
    public static int getDimensionIDForWorld(WorldServer world) {
        WorldServer[] worlds = FMLCommonHandler.instance().getMinecraftServerInstance().worlds;

        return Arrays.asList(worlds).indexOf(world);
    }

    /**
     * Gets the TileEntity that exists at the given position
     * @param position The position
     * @return The TileEntity that exists at the given position, or null if non exists
     */
    @Nullable
    public static TileEntity getTileEntityAtExtendedPosition(ExtendedBlockPos position) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(position.getDimID()).getTileEntity(position);
    }

    /**
     * Destroys a TileEntityClaimer
     * @param position The position of the claimer
     * @return true if the claimer was destroyed, false otherwise
     */
    @CheckReturnValue
    public static boolean destroyClaimer(ExtendedBlockPos position) {
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(position.getDimID());
        return destroyClaimer(world, position);
    }

    /**
     * Destroys a TileEntityClaimer
     * @param world The world the claimer exists in
     * @param position The position of the claimer
     * @return true if the claimer was destroyed, false otherwise
     */
    public static boolean destroyClaimer(World world, BlockPos position) {
        if(!(world.getTileEntity(position) instanceof TileEntityClaimer)) {
            return false;
        }

        // world.playSound(null, position, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 0.1f, 0.7f);
        world.createExplosion(null, position.getX(), position.getY(), position.getZ(), 0.7f, false);

        world.setBlockToAir(position);

        // Make sure that there is actually a _top_ part before we break it
        IBlockState top = world.getBlockState(position.up());
        if(top.getBlock() instanceof BlockClaimer && top.getValue(BlockClaimer.HALF) == BlockClaimer.ClaimerHalf.TOP) {
            world.setBlockToAir(position.up());
        }

        // Spawn an entityItem in the world to represent the now-destroyed claimer
        WarringStatesBlocks.BLOCK_CLAIMER.dropBlockAsItem(world, position, WarringStatesBlocks.BLOCK_CLAIMER.getDefaultState(), 0);

        return true;
    }

    public static Chunk getChunkFor(ExtendedBlockPos position) {
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(position.getDimID());
        return getChunkFor(world, position);
    }

    public static Chunk getChunkFor(World world, BlockPos position) {
        return world.getChunk(position);
    }

    public static ExtendedBlockPos getExtendedPosition(World world, BlockPos pos) {
        return new ExtendedBlockPos(pos, getDimensionIDForWorld((WorldServer)world));
    }

    public static BlockPos offsetBlockPos(@Nonnull BlockPos position, @Nullable EnumFacing side) {
        return side == null ? position : position.offset(side);
    }

    public static void notifySurroundingBlocksOfStateChange(World world, BlockPos pos, IBlockState newState) {
        world.notifyBlockUpdate(pos, newState, newState, 2);
        world.notifyBlockUpdate(pos.up(), newState, newState, 2);
        world.notifyBlockUpdate(pos.down(), newState, newState, 2);
        world.notifyBlockUpdate(pos.north(), newState, newState, 2);
        world.notifyBlockUpdate(pos.south(), newState, newState, 2);
        world.notifyBlockUpdate(pos.east(), newState, newState, 2);
        world.notifyBlockUpdate(pos.west(), newState, newState, 2);
    }

    public static boolean areChunksAdjacent(ChunkPos chunk1, ChunkPos chunk2) {
        int xdist = Math.abs(chunk1.x - chunk2.x);
        int zdist = Math.abs(chunk1.z - chunk2.z);

        return xdist <= 1 && zdist <= 1;
    }

    public static boolean doesChunkBorderWilderness(ChunkPos chunk, int dimension) {
        return StateManager.getInstance().getStateAtPosition(new ChunkPos(chunk.x + 1, chunk.z), dimension) == null ||
               StateManager.getInstance().getStateAtPosition(new ChunkPos(chunk.x - 1, chunk.z), dimension) == null ||
               StateManager.getInstance().getStateAtPosition(new ChunkPos(chunk.x, chunk.z + 1), dimension) == null ||
               StateManager.getInstance().getStateAtPosition(new ChunkPos(chunk.x, chunk.z - 1), dimension) == null;
    }

    public static boolean doesChunkBorderState(ChunkPos chunk, int dimension) {
        return StateManager.getInstance().getStateAtPosition(new ChunkPos(chunk.x + 1, chunk.z), dimension) != null ||
               StateManager.getInstance().getStateAtPosition(new ChunkPos(chunk.x - 1, chunk.z), dimension) != null ||
               StateManager.getInstance().getStateAtPosition(new ChunkPos(chunk.x, chunk.z + 1), dimension) != null ||
               StateManager.getInstance().getStateAtPosition(new ChunkPos(chunk.x, chunk.z - 1), dimension) != null;

    }
}
