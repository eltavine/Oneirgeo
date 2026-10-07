package com.eltavine.oneirgeo.world.gen.scene.nether;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.util.Noise;
import com.eltavine.oneirgeo.world.gen.scene.Scene;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import com.eltavine.oneirgeo.world.gen.scene.SceneInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The furnace went out a long time ago. Grey ash to the horizon, boilers the size of buildings lying
 * on their sides, and lava that froze while it was still falling from nowhere.
 */
public final class AshPlainsScene implements Scene {
    public static final int GROUND = -1210;
    private static final int BOILER_CELL = 150;
    private static final int FALL_CELL = 200;
    private static final BlockState ASH = OneirgeoBlocks.ASH.defaultBlockState();
    private static final BlockState BLACKSTONE = Blocks.BLACKSTONE.defaultBlockState();
    private static final BlockState BASALT = Blocks.BASALT.defaultBlockState();
    private static final BlockState PLATE = OneirgeoBlocks.BOILER_PLATE.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    public static int height(long seed, int x, int z) {
        return GROUND + (int) Math.round(Noise.fbm2(seed, x, z, 160.0, 2) * 5.0 + Noise.fbm2(seed + 5, x, z, 40.0, 2) * 1.5);
    }

    @Override
    public int surfaceY(SceneInfo info, int x, int z) {
        return height(info.seed(), x, z);
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
                int h = height(seed, x, z);
                for (int y = ctx.minY(); y <= h - 4; y++) {
                    ctx.set(lx, y, lz, Noise.value3(seed + 9, x / 9.0, y / 5.0, z / 9.0) > 0.35 ? BASALT : BLACKSTONE);
                }
                ctx.fill(lx, lz, h - 3, h, ASH);
                long roll = Hash.of(seed, x, z, 0xA5);
                if (Hash.chance(roll, 0.003)) {
                    ctx.set(lx, h, lz, OneirgeoBlocks.EMBER.defaultBlockState());
                } else if (Hash.chance(Hash.next(roll, 1), 0.0012)) {
                    ctx.set(lx, h, lz, Blocks.NETHERRACK.defaultBlockState());
                    ctx.set(lx, h + 1, lz, Blocks.FIRE.defaultBlockState());
                }
            }
        }
        this.boilers(ctx);
        this.lavafalls(ctx);
        long dimensionSeed = ctx.sampler().seed();
        for (NetherFeatures.Sinkhole hole : NetherFeatures.sinkholesNear(dimensionSeed, ctx.originX(), ctx.originZ(), ctx.originX() + 15, ctx.originZ() + 15)) {
            if (ctx.anchorOwned(hole.x(), hole.z())) {
                this.sinkhole(ctx, hole, height(seed, hole.x(), hole.z()));
            }
        }
    }

    private void sinkhole(SceneContext ctx, NetherFeatures.Sinkhole hole, int top) {
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
        for (int y = ctx.minY(); y <= top + 1; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    ctx.place(hole.x() + dx, y, hole.z() + dz, dz == -1 && dx == 0 ? BLACKSTONE : (dz == 0 && dx == 0 ? ladder : AIR));
                }
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    ctx.place(hole.x() + dx, top + 1, hole.z() + dz, Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());
                }
            }
        }
    }

    private void boilers(SceneContext ctx) {
        long seed = ctx.seed();
        int minX = ctx.originX();
        int minZ = ctx.originZ();
        for (int cz = Math.floorDiv(minZ - 50, BOILER_CELL); cz <= Math.floorDiv(minZ + 65, BOILER_CELL); cz++) {
            for (int cx = Math.floorDiv(minX - 50, BOILER_CELL); cx <= Math.floorDiv(minX + 65, BOILER_CELL); cx++) {
                long h = Hash.of(seed, cx, cz, 0xB011);
                if (!Hash.chance(h, 0.4)) {
                    continue;
                }
                int bx = cx * BOILER_CELL + Hash.range(Hash.next(h, 1), 40, BOILER_CELL - 40);
                int bz = cz * BOILER_CELL + Hash.range(Hash.next(h, 2), 40, BOILER_CELL - 40);
                if (!ctx.anchorOwned(bx, bz)) {
                    continue;
                }
                boolean alongX = Hash.chance(Hash.next(h, 3), 0.5);
                int radius = Hash.range(Hash.next(h, 4), 5, 9);
                int half = Hash.range(Hash.next(h, 5), 15, 35);
                int cy = height(seed, bx, bz) + radius - 3;
                for (int x = Math.max(minX, bx - (alongX ? half : radius)); x <= Math.min(minX + 15, bx + (alongX ? half : radius)); x++) {
                    for (int z = Math.max(minZ, bz - (alongX ? radius : half)); z <= Math.min(minZ + 15, bz + (alongX ? radius : half)); z++) {
                        int along = alongX ? x - bx : z - bz;
                        int across = alongX ? z - bz : x - bx;
                        for (int y = cy - radius; y <= cy + radius; y++) {
                            double d = Math.sqrt(across * across + (y - cy) * (y - cy));
                            if (d > radius + 0.5) {
                                continue;
                            }
                            boolean cap = Math.abs(along) >= half - 1;
                            boolean shell = d >= radius - 1.5 || cap;
                            boolean rusted = Noise.value3(seed + 77, x / 4.0, y / 4.0, z / 4.0) > 0.45;
                            if (shell && !rusted) {
                                ctx.place(x, y, z, Math.floorMod(along, 8) == 0 ? Blocks.IRON_BLOCK.defaultBlockState() : PLATE);
                            } else if (!shell) {
                                ctx.place(x, y, z, AIR);
                            }
                        }
                    }
                }
            }
        }
    }

    /** Lava that froze mid-fall: tall curved sheets standing on the ash, thinning towards a ragged top. */
    private void lavafalls(SceneContext ctx) {
        long seed = ctx.seed();
        int minX = ctx.originX();
        int minZ = ctx.originZ();
        for (int cz = Math.floorDiv(minZ - 60, FALL_CELL); cz <= Math.floorDiv(minZ + 75, FALL_CELL); cz++) {
            for (int cx = Math.floorDiv(minX - 60, FALL_CELL); cx <= Math.floorDiv(minX + 75, FALL_CELL); cx++) {
                long h = Hash.of(seed, cx, cz, 0xFA11);
                if (!Hash.chance(h, 0.35)) {
                    continue;
                }
                int fx = cx * FALL_CELL + Hash.range(Hash.next(h, 1), 40, FALL_CELL - 40);
                int fz = cz * FALL_CELL + Hash.range(Hash.next(h, 2), 40, FALL_CELL - 40);
                if (!ctx.anchorOwned(fx, fz)) {
                    continue;
                }
                double angle = Hash.unit(Hash.next(h, 3)) * Math.PI;
                double ax = Math.cos(angle);
                double az = Math.sin(angle);
                int width = Hash.range(Hash.next(h, 4), 6, 15);
                int height = Hash.range(Hash.next(h, 5), 60, 220);
                double amp = 2.0 + Hash.unit(Hash.next(h, 6)) * 4.0;
                double lean = (Hash.unit(Hash.next(h, 7)) - 0.5) * 0.12;
                int base = height(seed, fx, fz) - 2;
                int reach = width + (int) amp + (int) Math.ceil(Math.abs(lean) * height) + 2;
                for (int x = Math.max(minX, fx - reach); x <= Math.min(minX + 15, fx + reach); x++) {
                    for (int z = Math.max(minZ, fz - reach); z <= Math.min(minZ + 15, fz + reach); z++) {
                        double dx = x - fx;
                        double dz = z - fz;
                        double s = dx * ax + dz * az;
                        double p = -dx * az + dz * ax;
                        if (Math.abs(s) > width) {
                            continue;
                        }
                        double ragged = 0.75 + 0.25 * (Noise.value2(seed + 3, s / 3.0, cx * 13.0) * 0.5 + 0.5);
                        double centre = amp * Math.sin(s / width * Math.PI * 1.5);
                        for (int y = base; y <= base + height * ragged; y++) {
                            double t = (y - base) / (double) height;
                            double thickness = 1.6 * (1.0 - t * 0.6);
                            if (Math.abs(p - centre - (y - base) * lean) <= thickness) {
                                long b = Hash.of(seed, x, y, z);
                                BlockState state = Hash.chance(b, 0.12) ? Blocks.MAGMA_BLOCK.defaultBlockState()
                                        : (Hash.chance(Hash.next(b, 1), 0.03) ? Blocks.OBSIDIAN.defaultBlockState() : OneirgeoBlocks.COLD_LAVA.defaultBlockState());
                                ctx.place(x, y, z, state);
                            }
                        }
                    }
                }
            }
        }
    }
}
