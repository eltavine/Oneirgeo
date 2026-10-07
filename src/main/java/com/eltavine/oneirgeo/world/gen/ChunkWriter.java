package com.eltavine.oneirgeo.world.gen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

/**
 * Writes straight into chunk sections during {@code buildTerrain}. Heightmaps are primed afterwards,
 * so skipping {@link ChunkAccess#setBlockState} is safe and avoids per-block bookkeeping on 254 sections.
 */
public final class ChunkWriter {
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private final ChunkAccess chunk;
    private final LevelChunkSection[] sections;
    private final int minY;
    private final int maxY;
    private final int minBlockX;
    private final int minBlockZ;

    public ChunkWriter(ChunkAccess chunk) {
        this.chunk = chunk;
        this.sections = chunk.getSections();
        this.minY = chunk.getMinY();
        this.maxY = chunk.getMaxY();
        this.minBlockX = chunk.getPos().getMinBlockX();
        this.minBlockZ = chunk.getPos().getMinBlockZ();
    }

    public ChunkAccess chunk() {
        return this.chunk;
    }

    public int minY() {
        return this.minY;
    }

    public int maxY() {
        return this.maxY;
    }

    public int minBlockX() {
        return this.minBlockX;
    }

    public int minBlockZ() {
        return this.minBlockZ;
    }

    public void set(int localX, int y, int localZ, BlockState state) {
        if (y < this.minY || y > this.maxY) {
            return;
        }
        LevelChunkSection section = this.sections[(y - this.minY) >> 4];
        section.setBlockState(localX, y & 15, localZ, state, false);
    }

    public BlockState get(int localX, int y, int localZ) {
        if (y < this.minY || y > this.maxY) {
            return AIR;
        }
        return this.sections[(y - this.minY) >> 4].getBlockState(localX, y & 15, localZ);
    }

    /** Sets a block given world coordinates; ignored when outside this chunk. */
    public void setWorld(int x, int y, int z, BlockState state) {
        int lx = x - this.minBlockX;
        int lz = z - this.minBlockZ;
        if (lx < 0 || lx > 15 || lz < 0 || lz > 15) {
            return;
        }
        this.set(lx, y, lz, state);
    }

    public boolean containsWorld(int x, int z) {
        int lx = x - this.minBlockX;
        int lz = z - this.minBlockZ;
        return lx >= 0 && lx < 16 && lz >= 0 && lz < 16;
    }

    public void fillColumn(int localX, int localZ, int fromY, int toY, BlockState state) {
        int lo = Math.max(fromY, this.minY);
        int hi = Math.min(toY, this.maxY);
        for (int y = lo; y <= hi; y++) {
            this.sections[(y - this.minY) >> 4].setBlockState(localX, y & 15, localZ, state, false);
        }
    }
}
