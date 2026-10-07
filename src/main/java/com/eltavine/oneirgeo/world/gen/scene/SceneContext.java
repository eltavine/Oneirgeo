package com.eltavine.oneirgeo.world.gen.scene;

import com.eltavine.oneirgeo.space.Box;
import com.eltavine.oneirgeo.space.ChunkSpaceData;
import com.eltavine.oneirgeo.space.SeamVolume;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.ChunkWriter;
import com.eltavine.oneirgeo.world.gen.DimensionLayout;
import com.eltavine.oneirgeo.world.gen.LayoutSampler;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Terrain-writing view of one chunk, restricted to the columns and height range of one scene. */
public final class SceneContext {
    private final SceneInfo info;
    private final LayoutSampler sampler;
    private final ChunkWriter writer;
    private final ChunkSpaceData.Builder space;
    private final boolean[] owned;
    private final LayoutSampler.Region[] regions;
    private final int minY;
    private final int maxY;

    public SceneContext(SceneInfo info, LayoutSampler sampler, ChunkWriter writer, ChunkSpaceData.Builder space,
                        boolean[] owned, LayoutSampler.Region[] regions) {
        this.info = info;
        this.sampler = sampler;
        this.writer = writer;
        this.space = space;
        this.owned = owned;
        this.regions = regions;
        this.minY = Math.max(info.layer().minY(), writer.minY());
        this.maxY = Math.min(info.layer().maxY(), writer.maxY());
    }

    public SceneInfo info() {
        return this.info;
    }

    public long seed() {
        return this.info.seed();
    }

    public DimensionLayout.Layer layer() {
        return this.info.layer();
    }

    public LayoutSampler sampler() {
        return this.sampler;
    }

    public ChunkWriter writer() {
        return this.writer;
    }

    /** Lowest Y this scene may write to. */
    public int minY() {
        return this.minY;
    }

    /** Highest Y (inclusive) this scene may write to. */
    public int maxY() {
        return this.maxY;
    }

    public int originX() {
        return this.writer.minBlockX();
    }

    public int originZ() {
        return this.writer.minBlockZ();
    }

    public int chunkX() {
        return this.writer.minBlockX() >> 4;
    }

    public int chunkZ() {
        return this.writer.minBlockZ() >> 4;
    }

    public boolean owns(int localX, int localZ) {
        return this.owned[localX | localZ << 4];
    }

    public boolean ownsWorld(int x, int z) {
        int lx = x - this.writer.minBlockX();
        int lz = z - this.writer.minBlockZ();
        return lx >= 0 && lx < 16 && lz >= 0 && lz < 16 && this.owned[lx | lz << 4];
    }

    public LayoutSampler.Region region(int localX, int localZ) {
        return this.regions[localX | localZ << 4];
    }

    public void set(int localX, int y, int localZ, BlockState state) {
        if (y >= this.minY && y <= this.maxY && this.owned[localX | localZ << 4]) {
            this.writer.set(localX, y, localZ, state);
        }
    }

    public void setWorld(int x, int y, int z, BlockState state) {
        int lx = x - this.writer.minBlockX();
        int lz = z - this.writer.minBlockZ();
        if (lx >= 0 && lx < 16 && lz >= 0 && lz < 16) {
            this.set(lx, y, lz, state);
        }
    }

    public BlockState get(int localX, int y, int localZ) {
        return this.writer.get(localX, y, localZ);
    }

    /** True when the region at a column belongs to this scene; structures are built by the owner of their anchor. */
    public boolean anchorOwned(int x, int z) {
        return this.sampler.region(this.info.layerIndex(), x, z).entryIndex() == this.info.region().entryIndex();
    }

    /** Structure write: clipped to the chunk and the layer, but not to region ownership. */
    public void place(int x, int y, int z, BlockState state) {
        int lx = x - this.writer.minBlockX();
        int lz = z - this.writer.minBlockZ();
        if (lx >= 0 && lx < 16 && lz >= 0 && lz < 16 && y >= this.minY && y <= this.maxY) {
            this.writer.set(lx, y, lz, state);
        }
    }

    public void placeBox(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        int ox = this.writer.minBlockX();
        int oz = this.writer.minBlockZ();
        int ax = Math.max(Math.min(x0, x1), ox);
        int bx = Math.min(Math.max(x0, x1), ox + 15);
        int az = Math.max(Math.min(z0, z1), oz);
        int bz = Math.min(Math.max(z0, z1), oz + 15);
        int ya = Math.max(Math.min(y0, y1), this.minY);
        int yb = Math.min(Math.max(y0, y1), this.maxY);
        for (int z = az; z <= bz; z++) {
            for (int x = ax; x <= bx; x++) {
                for (int y = ya; y <= yb; y++) {
                    this.writer.set(x - ox, y, z - oz, state);
                }
            }
        }
    }

    public BlockState getWorld(int x, int y, int z) {
        int lx = x - this.writer.minBlockX();
        int lz = z - this.writer.minBlockZ();
        if (lx < 0 || lx > 15 || lz < 0 || lz > 15) {
            return Blocks.AIR.defaultBlockState();
        }
        return this.writer.get(lx, y, lz);
    }

    public void fill(int localX, int localZ, int fromY, int toY, BlockState state) {
        if (!this.owned[localX | localZ << 4]) {
            return;
        }
        this.writer.fillColumn(localX, localZ, Math.max(fromY, this.minY), Math.min(toY, this.maxY), state);
    }

    /** Fills a world-space box (inclusive bounds) clipped to this chunk and scene. */
    public void fillBox(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        int ox = this.writer.minBlockX();
        int oz = this.writer.minBlockZ();
        int lx0 = Math.max(Math.min(x0, x1) - ox, 0);
        int lx1 = Math.min(Math.max(x0, x1) - ox, 15);
        int lz0 = Math.max(Math.min(z0, z1) - oz, 0);
        int lz1 = Math.min(Math.max(z0, z1) - oz, 15);
        int ya = Math.min(y0, y1);
        int yb = Math.max(y0, y1);
        for (int lz = lz0; lz <= lz1; lz++) {
            for (int lx = lx0; lx <= lx1; lx++) {
                this.fill(lx, lz, ya, yb, state);
            }
        }
    }

    public boolean intersectsChunk(int x0, int z0, int x1, int z1) {
        int ox = this.writer.minBlockX();
        int oz = this.writer.minBlockZ();
        return Math.max(x0, x1) >= ox && Math.min(x0, x1) <= ox + 15 && Math.max(z0, z1) >= oz && Math.min(z0, z1) <= oz + 15;
    }

    public long hash(long a, long b) {
        return Hash.of(this.info.seed(), a, b);
    }

    public long hash(long a, long b, long c) {
        return Hash.of(this.info.seed(), a, b, c);
    }

    public void seam(SeamVolume seam) {
        this.space.seam(seam);
    }

    public void flipZone(Box box) {
        this.space.flip(box);
    }

    public void protect(Box box) {
        this.space.protect(box);
    }

    /** Heals removed blocks inside the box but lets anything be built there. */
    public void protectSolid(Box box) {
        this.space.protectSolid(box);
    }

    public void trap(Box box) {
        this.space.trap(box);
    }
}
