package com.eltavine.oneirgeo.world.gen.scene.nether;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.scene.Scene;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Two thousand blocks of hot air between the ash and the hanging city, with burning spheres, cubes and
 * octahedra drifting in it, some trailing chains that end in nothing.
 */
public final class GreatHearthScene implements Scene {
    private static final int CELL = 112;
    private static final BlockState NETHERRACK = Blocks.NETHERRACK.defaultBlockState();
    private static final BlockState MAGMA = Blocks.MAGMA_BLOCK.defaultBlockState();
    private static final BlockState BLACKSTONE = Blocks.BLACKSTONE.defaultBlockState();
    private static final BlockState FIRE = Blocks.FIRE.defaultBlockState();
    private static final BlockState CHAIN = Blocks.IRON_CHAIN.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Y);

    @Override
    public void generate(SceneContext ctx) {
        long seed = ctx.seed();
        int minX = ctx.originX();
        int minZ = ctx.originZ();
        int y0 = Math.floorDiv(ctx.minY(), CELL);
        int y1 = Math.floorDiv(ctx.maxY(), CELL);
        for (int cz = Math.floorDiv(minZ - 40, CELL); cz <= Math.floorDiv(minZ + 55, CELL); cz++) {
            for (int cx = Math.floorDiv(minX - 40, CELL); cx <= Math.floorDiv(minX + 55, CELL); cx++) {
                for (int cy = y0; cy <= y1; cy++) {
                    long h = Hash.of(seed, cx, cy, cz);
                    if (!Hash.chance(h, 0.3)) {
                        continue;
                    }
                    int radius = Hash.range(Hash.next(h, 1), 6, 26);
                    int bx = cx * CELL + Hash.range(Hash.next(h, 2), radius + 4, CELL - radius - 4);
                    int by = cy * CELL + Hash.range(Hash.next(h, 3), radius + 4, CELL - radius - 4);
                    int bz = cz * CELL + Hash.range(Hash.next(h, 4), radius + 4, CELL - radius - 4);
                    if (by - radius < ctx.minY() + 4 || by + radius > ctx.maxY() - 4) {
                        continue;
                    }
                    int shape = Hash.range(Hash.next(h, 5), 0, 2);
                    int chain = Hash.chance(Hash.next(h, 6), 0.35) ? Hash.range(Hash.next(h, 7), 20, 70) : 0;
                    this.body(ctx, seed, bx, by, bz, radius, shape, chain);
                }
            }
        }
    }

    private void body(SceneContext ctx, long seed, int bx, int by, int bz, int radius, int shape, int chain) {
        int minX = ctx.originX();
        int minZ = ctx.originZ();
        for (int x = Math.max(minX, bx - radius); x <= Math.min(minX + 15, bx + radius); x++) {
            for (int z = Math.max(minZ, bz - radius); z <= Math.min(minZ + 15, bz + radius); z++) {
                int top = Integer.MIN_VALUE;
                for (int y = by - radius; y <= by + radius; y++) {
                    double dx = Math.abs(x - bx);
                    double dy = Math.abs(y - by);
                    double dz = Math.abs(z - bz);
                    double d = switch (shape) {
                        case 0 -> Math.sqrt(dx * dx + dy * dy + dz * dz);
                        case 1 -> Math.max(dx, Math.max(dy, dz));
                        default -> dx + dy + dz;
                    };
                    if (d > radius || Hash.chance(Hash.of(seed, x, y, z, 0x401E), 0.08)) {
                        continue;
                    }
                    BlockState state;
                    if (shape == 1) {
                        state = Hash.chance(Hash.of(seed, x, y, z), 0.06) ? OneirgeoBlocks.EMBER.defaultBlockState() : BLACKSTONE;
                    } else {
                        state = d > radius - 1.6 ? MAGMA : NETHERRACK;
                    }
                    ctx.place(x, y, z, state);
                    top = Math.max(top, y);
                }
                if (top != Integer.MIN_VALUE && shape != 1 && Hash.chance(Hash.of(seed, x, top, z, 0xF1), 0.35)) {
                    ctx.place(x, top, z, NETHERRACK);
                    ctx.place(x, top + 1, z, FIRE);
                }
            }
        }
        if (chain > 0) {
            for (int y = by - radius - chain; y < by - radius + 1; y++) {
                ctx.place(bx, y, bz, CHAIN);
            }
        }
    }
}
