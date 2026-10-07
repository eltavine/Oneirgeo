package com.eltavine.oneirgeo.world.gen.scene.end;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.scene.Frame;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A faceless mourner tens of blocks tall, hands folded and head bowed; some kneel, some have lost
 * their heads, which lie in the dust beside them.
 */
final class Colossus {
    private static final BlockState STONE = OneirgeoBlocks.STAR_STONE.defaultBlockState();
    private static final BlockState PLINTH = Blocks.END_STONE_BRICKS.defaultBlockState();

    private Colossus() {
    }

    static int height(EndIslands.Island island) {
        return Hash.range(Hash.next(island.hash(), 20), 30, 54);
    }

    /** Half-width of the ground the statue and its plinth occupy, so graves keep clear. */
    static int footprint(EndIslands.Island island) {
        return (int) (height(island) * 0.42) + 3;
    }

    static void build(SceneContext ctx, EndIslands.Island island) {
        int reach = footprint(island) + 4;
        if (!ctx.intersectsChunk(island.x() - reach, island.z() - reach, island.x() + reach, island.z() + reach)) {
            return;
        }
        int h = height(island);
        long hash = island.hash();
        boolean kneeling = Hash.chance(Hash.next(hash, 21), 0.35);
        boolean headless = Hash.chance(Hash.next(hash, 22), 0.25);
        Frame f = new Frame(island.x(), island.top(), island.z(), island.facing());
        int w = (int) (h * 0.22);
        box(ctx, f, -w, w, 1, 2, -w, w, PLINTH, hash);

        int base = 3;
        int drop = 0;
        if (kneeling) {
            drop = (int) (h * 0.26);
            box(ctx, f, -s(h, 0.17), s(h, 0.17), base, base + s(h, 0.12), -s(h, 0.07), s(h, 0.24), STONE, hash);
        } else {
            box(ctx, f, -s(h, 0.17), -s(h, 0.05), base, s(h, 0.46), -s(h, 0.06), s(h, 0.06), STONE, hash);
            box(ctx, f, s(h, 0.05), s(h, 0.17), base, s(h, 0.46), -s(h, 0.06), s(h, 0.06), STONE, hash);
        }
        int y = -drop;
        box(ctx, f, -s(h, 0.2), s(h, 0.2), y + s(h, 0.36), y + s(h, 0.78), -s(h, 0.09), s(h, 0.09), STONE, hash);
        box(ctx, f, -s(h, 0.29), -s(h, 0.2), y + s(h, 0.5), y + s(h, 0.76), -s(h, 0.05), s(h, 0.05), STONE, hash);
        box(ctx, f, s(h, 0.2), s(h, 0.29), y + s(h, 0.5), y + s(h, 0.76), -s(h, 0.05), s(h, 0.05), STONE, hash);
        box(ctx, f, -s(h, 0.12), s(h, 0.12), y + s(h, 0.58), y + s(h, 0.66), s(h, 0.05), s(h, 0.16), STONE, hash);
        if (headless) {
            int a = s(h, 0.3);
            int b = s(h, 0.25);
            int r = s(h, 0.08);
            box(ctx, f, a - r, a + r, 1, 2 * r + 1, b - r, b + r, STONE, hash);
        } else {
            box(ctx, f, -s(h, 0.08), s(h, 0.08), y + s(h, 0.8), y + s(h, 0.94), s(h, 0.0), s(h, 0.15), STONE, hash);
            box(ctx, f, -s(h, 0.1), s(h, 0.1), y + s(h, 0.86), y + s(h, 0.97), -s(h, 0.04), s(h, 0.12), STONE, hash);
        }
    }

    private static int s(int h, double fraction) {
        return (int) Math.round(h * fraction);
    }

    /** Inclusive local box; a few blocks are worn away. */
    private static void box(SceneContext ctx, Frame f, int a0, int a1, int y0, int y1, int b0, int b1, BlockState state, long hash) {
        int xa = f.worldX(a0, b0);
        int xb = f.worldX(a1, b1);
        int za = f.worldZ(a0, b0);
        int zb = f.worldZ(a1, b1);
        int x0 = Math.min(xa, xb);
        int x1 = Math.max(xa, xb);
        int z0 = Math.min(za, zb);
        int z1 = Math.max(za, zb);
        if (!ctx.intersectsChunk(x0, z0, x1, z1)) {
            return;
        }
        for (int x = Math.max(x0, ctx.originX()); x <= Math.min(x1, ctx.originX() + 15); x++) {
            for (int z = Math.max(z0, ctx.originZ()); z <= Math.min(z1, ctx.originZ() + 15); z++) {
                for (int y = f.y() + y0; y <= f.y() + y1; y++) {
                    if (state != PLINTH && Hash.chance(Hash.of(hash, x, y, z), 0.1)) {
                        continue;
                    }
                    ctx.place(x, y, z, state);
                }
            }
        }
    }
}
