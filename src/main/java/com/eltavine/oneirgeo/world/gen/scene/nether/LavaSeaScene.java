package com.eltavine.oneirgeo.world.gen.scene.nether;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.util.Noise;
import com.eltavine.oneirgeo.world.gen.scene.Scene;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import com.eltavine.oneirgeo.world.gen.scene.SceneInfo;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** The last of the heat, a slow lava sea with a few black islands. */
public final class LavaSeaScene implements Scene {
    public static final int SURFACE = -1214;
    private static final int FLOOR = -1240;
    private static final BlockState LAVA = Blocks.LAVA.defaultBlockState();
    private static final BlockState BASALT = Blocks.BASALT.defaultBlockState();
    private static final BlockState BLACKSTONE = Blocks.BLACKSTONE.defaultBlockState();

    private static int island(long seed, int x, int z) {
        double n = Noise.fbm2(seed, x, z, 90.0, 3);
        return n > 0.45 ? SURFACE + (int) Math.round((n - 0.45) * 40.0) : Integer.MIN_VALUE;
    }

    @Override
    public int surfaceY(SceneInfo info, int x, int z) {
        return island(info.seed(), x, z);
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
                ctx.fill(lx, lz, ctx.minY(), FLOOR, BASALT);
                int top = island(seed, x, z);
                if (top != Integer.MIN_VALUE) {
                    ctx.fill(lx, lz, FLOOR + 1, top - 1, BLACKSTONE);
                    ctx.set(lx, top, lz, OneirgeoBlocks.ASH.defaultBlockState());
                } else {
                    ctx.fill(lx, lz, FLOOR + 1, SURFACE, LAVA);
                }
            }
        }
    }
}
