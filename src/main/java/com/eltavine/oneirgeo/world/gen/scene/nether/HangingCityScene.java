package com.eltavine.oneirgeo.world.gen.scene.nether;

import com.eltavine.oneirgeo.space.Box;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.scene.Scene;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import com.eltavine.oneirgeo.world.gen.scene.SceneInfo;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * A city hanging from the ceiling of the world. Below the ceiling gravity points up, so its streets
 * are the underside of the rock and its towers go down; to the people who lived there they went up.
 */
public final class HangingCityScene implements Scene {
    public static final int CEILING = 1904;
    public static final int FLIP_BOTTOM = 1480;
    private static final int PLOT = 40;
    private static final int STREET = 8;
    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
    private static final BlockState ROCK = Blocks.BLACKSTONE.defaultBlockState();
    private static final BlockState BASALT = Blocks.BASALT.defaultBlockState();
    private static final BlockState WALL = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState TRIM = Blocks.NETHER_BRICKS.defaultBlockState();
    private static final BlockState WINDOW = Blocks.SHROOMLIGHT.defaultBlockState();
    private static final BlockState DARK_WINDOW = Blocks.TINTED_GLASS.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    /** One tower of a plot: a box hanging down from the ceiling, {@code depth} blocks deep. */
    private record Tower(int x0, int z0, int x1, int z1, int depth, int salt) {
        boolean contains(int x, int z) {
            return x >= this.x0 && x <= this.x1 && z >= this.z0 && z <= this.z1;
        }
    }

    private static Tower[] towers(long seed, int px, int pz) {
        long h = Hash.of(seed, px, pz, 0xC17E);
        if (!Hash.chance(h, 0.85)) {
            return new Tower[0];
        }
        int baseX = px * PLOT + STREET + 2;
        int baseZ = pz * PLOT + STREET + 2;
        int span = PLOT - STREET - 4;
        int count = Hash.range(Hash.next(h, 1), 1, 2);
        Tower[] result = new Tower[count];
        for (int i = 0; i < count; i++) {
            long t = Hash.next(h, 10 + i);
            int w = Hash.range(Hash.next(t, 1), 8, span / count + 4);
            int d = Hash.range(Hash.next(t, 2), 8, span);
            int ox = i == 0 ? 0 : span - w;
            int oz = Hash.range(Hash.next(t, 3), 0, Math.max(0, span - d));
            result[i] = new Tower(baseX + ox, baseZ + oz, baseX + Math.min(span, ox + w), baseZ + Math.min(span, oz + d),
                    Hash.range(Hash.next(t, 4), 16, 96), (int) (t & 0xFFFF));
        }
        return result;
    }

    @Override
    public int surfaceY(SceneInfo info, int x, int z) {
        return Integer.MIN_VALUE;
    }

    @Override
    public void generate(SceneContext ctx) {
        long seed = ctx.seed();
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                if (!ctx.owns(lx, lz)) {
                    continue;
                }
                int x = ctx.originX() + lx;
                int z = ctx.originZ() + lz;
                ctx.set(lx, ctx.maxY(), lz, BEDROCK);
                for (int y = CEILING; y < ctx.maxY(); y++) {
                    ctx.set(lx, y, lz, Math.floorMod(y + (x >> 3) + (z >> 3), 7) == 0 ? BASALT : ROCK);
                }
                int px = Math.floorDiv(x, PLOT);
                int pz = Math.floorDiv(z, PLOT);
                int u = Math.floorMod(x, PLOT);
                int v = Math.floorMod(z, PLOT);
                if (u < STREET || v < STREET) {
                    this.street(ctx, lx, lz, u, v);
                    continue;
                }
                for (Tower tower : towers(seed, px, pz)) {
                    if (tower.contains(x, z)) {
                        this.towerColumn(ctx, seed, lx, lz, x, z, tower);
                    }
                }
            }
        }
        ctx.flipZone(new Box(ctx.originX(), FLIP_BOTTOM, ctx.originZ(), ctx.originX() + 16, CEILING, ctx.originZ() + 16));
    }

    /** Lamps hang from the ceiling at street crossings; to an upside-down walker they are lamp posts. */
    private void street(SceneContext ctx, int lx, int lz, int u, int v) {
        if ((u == 3 || u == 4) && (v == 3 || v == 4)) {
            if (u == 3 && v == 3) {
                for (int y = CEILING - 5; y < CEILING; y++) {
                    ctx.set(lx, y, lz, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
                }
                ctx.set(lx, CEILING - 6, lz, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
            return;
        }
        if (u == 0 || v == 0) {
            ctx.set(lx, CEILING - 1, lz, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.TOP));
        }
    }

    private void towerColumn(SceneContext ctx, long seed, int lx, int lz, int x, int z, Tower t) {
        boolean edge = x == t.x0() || x == t.x1() || z == t.z0() || z == t.z1();
        boolean corner = (x == t.x0() || x == t.x1()) && (z == t.z0() || z == t.z1());
        int bottom = CEILING - t.depth();
        for (int y = bottom; y < CEILING; y++) {
            int fromCeiling = CEILING - 1 - y;
            BlockState state;
            if (edge) {
                boolean windowRow = fromCeiling % 6 == 3 || fromCeiling % 6 == 4;
                boolean door = fromCeiling <= 2 && fromCeiling >= 1 && !corner && (x == t.x0() && Math.floorMod(z - t.z0(), 9) == 4);
                if (door) {
                    state = AIR;
                } else if (windowRow && !corner && Math.floorMod(x + z, 3) != 0) {
                    state = Hash.chance(Hash.of(seed, x, y / 6, z, t.salt()), 0.25) ? WINDOW : DARK_WINDOW;
                } else if (!corner && fromCeiling >= 3 && Hash.chance(Hash.of(seed, x, y, z, 0x401E), 0.1)) {
                    state = AIR;
                } else {
                    state = corner ? TRIM : WALL;
                }
            } else {
                boolean hatch = Math.floorMod(x - t.x0(), 6) == 3 && Math.floorMod(z - t.z0(), 6) == 3;
                state = fromCeiling % 6 == 5 && !hatch ? WALL : AIR;
            }
            ctx.set(lx, y, lz, state);
        }
        if (!edge) {
            int spike = (int) (Hash.unit(Hash.of(seed, x, z, t.salt())) * 3);
            for (int y = bottom - spike; y < bottom; y++) {
                ctx.set(lx, y, lz, BASALT);
            }
            ctx.set(lx, bottom, lz, TRIM);
        }
    }
}
