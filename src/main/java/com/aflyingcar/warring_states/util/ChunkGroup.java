package com.aflyingcar.warring_states.util;

import com.aflyingcar.warring_states.WarringStatesMod;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.Set;

/**
 * A helper class for representing a grouping of Chunk Positions
 */
public class ChunkGroup implements ISerializable {
    private final Set<ChunkPos> chunks;
    private int dimension;

    public ChunkGroup() {
        chunks = new HashSet<>();
        dimension = 0;
    }

    public ChunkGroup(int dimension) {
        this.chunks = new HashSet<>();
        this.dimension = dimension;
    }

    public void addChunk(ChunkPos pos) {
        chunks.add(pos);
    }

    public Set<ChunkPos> getChunks() {
        return chunks;
    }

    public boolean isEmpty() {
        return chunks.isEmpty();
    }

    /**
     * Checks if a {@code ChunkPos} is nearby (close to) this grouping
     * @param pos The {@code ChunkPos} to check
     * @return {@code true} if pos is nearby, {@code false} otherwise.
     */
    public boolean isChunkNearby(ChunkPos pos) {
        return chunks.parallelStream().anyMatch(c -> WorldUtils.areChunksAdjacent(c, pos));
    }

    @Override
    public NBTTagCompound writeNBT(NBTTagCompound nbt) {
        NBTTagList nbtChunks = new NBTTagList();
        for(ChunkPos chunk : chunks) {
            NBTTagCompound nbtChunk = new NBTTagCompound();
            nbtChunk.setInteger("x", chunk.x);
            nbtChunk.setInteger("z", chunk.z);
            nbtChunks.appendTag(nbtChunk);
        }
        nbt.setTag("chunks", nbtChunks);
        nbt.setInteger("dimension", dimension);

        return nbt;
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        if(!nbt.hasKey("chunks")) {
            WarringStatesMod.getLogger().error("NBTTagCompound missing required tag 'chunks'. Cannot load chunk group data.");
            return;
        }

        NBTTagList nbtChunks = nbt.getTagList("chunks", 10);
        int i = 0;
        for(NBTBase nbtBase : nbtChunks) {
            NBTTagCompound nbtChunk = (NBTTagCompound)nbtBase;

            if(nbtChunk.hasKey("x") && nbtChunk.hasKey("z")) {
                chunks.add(new ChunkPos(nbtChunk.getInteger("x"), nbtChunk.getInteger("z")));
            } else {
                WarringStatesMod.getLogger().error("Saved chunk #" + i + " is missing the 'x' or the 'z' tag! Skipping this chunk.");
            }
            ++i;
        }

        dimension = nbt.getInteger("dimension");
    }

    public int getDimension() {
        return dimension;
    }

    public void removeChunk(ChunkPos pos) {
        chunks.remove(pos);
    }

    public boolean containsChunk(ChunkPos chunkPos, int dimension) {
        return (this.dimension == dimension) && chunks.contains(chunkPos);
    }

    public boolean containsBlock(ExtendedBlockPos pos) {
        return (dimension == pos.getDimID()) && chunks.parallelStream().anyMatch(cpos -> WorldUtils.isBlockWithinChunk(cpos, pos));
    }
}
