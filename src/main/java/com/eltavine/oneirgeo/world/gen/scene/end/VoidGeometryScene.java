package com.eltavine.oneirgeo.world.gen.scene.end;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.scene.Scene;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Above the cemetery, the void is furnished with perfect solids: cubes, wireframes, octahedra, hollow
 * spheres, columns and rings, and straight staircases that climb out of nothing into nothing.
 */
public final class VoidGeometryScene implements Scene {
    private static final int CELL = 120;
    private static final int BODY_MARGIN = 40;
    private static final int STAIR_MARGIN = 100;
    private static final BlockState STAR = OneirgeoBlocks.STAR_STONE.defaultBlockState();
    private static final BlockState BRICKS = Blocks.END_STONE_BRICKS.defaultBlockState();
    private static final BlockState PURPUR = Blocks.PURPUR_BLOCK.defaultBlockState();
    private static final BlockState QUARTZ = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState LIGHT = Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState();
    private static final BlockState OBSIDIAN = Blocks.OBSIDIAN.defaultBlockState();
    private static final BlockState[] MATERIALS = {STAR, STAR, QUARTZ, BRICKS, PURPUR, OBSIDIAN};

    @Override
    public void generate(SceneContext ctx) {
        long seed = ctx.seed();
        int minX = ctx.originX();
        int minZ = ctx.originZ();
        int y0 = Math.floorDiv(ctx.minY(), CELL);
        int y1 = Math.floorDiv(ctx.maxY(), CELL);
        for (int cz = Math.floorDiv(minZ - STAIR_MARGIN, CELL); cz <= Math.floorDiv(minZ + 15 + STAIR_MARGIN, CELL); cz++) {
            for (int cx = Math.floorDiv(minX - STAIR_MARGIN, CELL); cx <= Math.floorDiv(minX + 15 + STAIR_MARGIN, CELL); cx++) {
                boolean bodyRange = cx >= Math.floorDiv(minX - BODY_MARGIN, CELL) && cx <= Math.floorDiv(minX + 15 + BODY_MARGIN, CELL)
                        && cz >= Math.floorDiv(minZ - BODY_MARGIN, CELL) && cz <= Math.floorDiv(minZ + 15 + BODY_MARGIN, CELL);
                for (int cy = y0; cy <= y1; cy++) {
                    long h = Hash.of(seed, cx, cy, cz);
                    if (bodyRange && Hash.chance(h, 0.34)) {
                        this.body(ctx, cx, cy, cz, h);
                    }
                    long s = Hash.next(h, 9);
                    if (Hash.chance(s, 0.22)) {
                        this.stair(ctx, cx, cy, cz, s);
                    }
                }
            }
        }
    }

    private void body(SceneContext ctx, int cx, int cy, int cz, long h) {
        int r = Hash.range(Hash.next(h, 1), 6, 30);
        int shape = Hash.range(Hash.next(h, 5), 0, 5);
        int reach = shape == 5 ? r + r / 5 + 1 : r;
        int bx = cx * CELL + Hash.range(Hash.next(h, 2), reach + 4, CELL - reach - 4);
        int by = cy * CELL + Hash.range(Hash.next(h, 3), reach + 4, CELL - reach - 4);
        int bz = cz * CELL + Hash.range(Hash.next(h, 4), reach + 4, CELL - reach - 4);
        if (by - reach < ctx.minY() + 4 || by + reach > ctx.maxY() - 4
                || !ctx.intersectsChunk(bx - reach, bz - reach, bx + reach, bz + reach)) {
            return;
        }
        BlockState material = MATERIALS[Hash.range(Hash.next(h, 6), 0, MATERIALS.length - 1)];
        int minX = ctx.originX();
        int minZ = ctx.originZ();
        for (int x = Math.max(minX, bx - reach); x <= Math.min(minX + 15, bx + reach); x++) {
            for (int z = Math.max(minZ, bz - reach); z <= Math.min(minZ + 15, bz + reach); z++) {
                for (int y = by - reach; y <= by + reach; y++) {
                    BlockState state = shapeAt(shape, r, x - bx, y - by, z - bz, material, h);
                    if (state != null && (state == LIGHT || !Hash.chance(Hash.of(h, x, y, z), 0.1))) {
                        ctx.place(x, y, z, state);
                    }
                }
            }
        }
    }

    private static BlockState shapeAt(int shape, int r, int dx, int dy, int dz, BlockState material, long h) {
        int ax = Math.abs(dx);
        int ay = Math.abs(dy);
        int az = Math.abs(dz);
        switch (shape) {
            case 0 -> {
                if (ax > r || ay > r || az > r) {
                    return null;
                }
                int edges = (ax == r ? 1 : 0) + (ay == r ? 1 : 0) + (az == r ? 1 : 0);
                return edges >= 2 ? LIGHT : material;
            }
            case 1 -> {
                int t = Math.max(1, r / 8);
                int near = (ax > r - t ? 1 : 0) + (ay > r - t ? 1 : 0) + (az > r - t ? 1 : 0);
                return ax <= r && ay <= r && az <= r && near >= 2 ? material : null;
            }
            case 2 -> {
                int d = ax + ay + az;
                if (d > r) {
                    return null;
                }
                return d < r / 3 ? LIGHT : material;
            }
            case 3 -> {
                double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (d > r || d < r - 2) {
                    return null;
                }
                return Hash.chance(Hash.of(h, dx >> 2, dy >> 2, dz >> 2), 0.18) ? null : material;
            }
            case 4 -> {
                int radius = Math.max(3, r / 2);
                if (dx * dx + dz * dz > radius * radius || ay > r) {
                    return null;
                }
                return Math.floorMod(dy, 12) == 0 ? LIGHT : material;
            }
            default -> {
                int minor = Math.max(2, r / 5);
                double ring = Math.sqrt(dx * dx + dz * dz) - r;
                if (ring * ring + dy * dy > minor * minor) {
                    return null;
                }
                return ay == 0 && Math.abs(ring) < 0.8 ? LIGHT : material;
            }
        }
    }

    /** A straight flight three wide, rising one block per block from a point in the cell. */
    private void stair(SceneContext ctx, int cx, int cy, int cz, long s) {
        Direction dir = Direction.from2DDataValue(Hash.range(Hash.next(s, 1), 0, 3));
        Direction side = dir.getClockWise();
        int length = Hash.range(Hash.next(s, 2), 24, 96);
        int sx = cx * CELL + Hash.range(Hash.next(s, 3), 4, CELL - 4);
        int sy = cy * CELL + Hash.range(Hash.next(s, 4), 8, CELL - 8);
        int sz = cz * CELL + Hash.range(Hash.next(s, 5), 4, CELL - 4);
        int ex = sx + dir.getStepX() * length;
        int ez = sz + dir.getStepZ() * length;
        if (sy < ctx.minY() + 2 || sy + length > ctx.maxY() - 2 || !ctx.intersectsChunk(sx - 3, sz - 3, ex + 3, ez + 3)) {
            return;
        }
        BlockState step = Blocks.PURPUR_STAIRS.defaultBlockState().setValue(StairBlock.FACING, dir);
        BlockState rod = Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP);
        for (int i = 0; i < length; i++) {
            int x = sx + dir.getStepX() * i;
            int z = sz + dir.getStepZ() * i;
            int y = sy + i;
            for (int w = -1; w <= 1; w++) {
                int wx = x + side.getStepX() * w;
                int wz = z + side.getStepZ() * w;
                if (w != 0 && Hash.chance(Hash.of(s, i, w), 0.15)) {
                    continue;
                }
                ctx.place(wx, y, wz, step);
                ctx.place(wx, y - 1, wz, BRICKS);
            }
            if (i % 12 == 6) {
                ctx.place(x + side.getStepX() * 2, y, z + side.getStepZ() * 2, BRICKS);
                ctx.place(x + side.getStepX() * 2, y + 1, z + side.getStepZ() * 2, rod);
            }
        }
    }
}
