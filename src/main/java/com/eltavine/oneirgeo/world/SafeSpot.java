package com.eltavine.oneirgeo.world;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.world.gen.OneirgeoChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Finds somewhere a player can stand, searching around a hint instead of scanning 4064 blocks. */
public final class SafeSpot {
    private SafeSpot() {
    }

    /** Best guess for the walkable surface of a layered dimension near {@code nearY}. */
    public static int surfaceHint(ServerLevel level, int x, int nearY, int z) {
        if (level.getChunkSource().getGenerator() instanceof OneirgeoChunkGenerator generator) {
            int y = generator.sampler().surfaceYNear(x, nearY, z);
            if (y != Integer.MIN_VALUE) {
                return y + 1;
            }
        }
        return nearY;
    }

    public static boolean canStand(ServerLevel level, BlockPos feet) {
        BlockState below = level.getBlockState(feet.below());
        if (!Block.isFaceFull(below.getCollisionShape(level, feet.below()), Direction.UP) && !below.is(OneirgeoBlocks.MIRROR_SURFACE)) {
            return false;
        }
        for (int i = 0; i < 2; i++) {
            BlockPos p = feet.above(i);
            BlockState state = level.getBlockState(p);
            if (!state.getCollisionShape(level, p).isEmpty() || !state.getFluidState().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** Searches a cube of columns around (x, nearY, z); returns the feet position or null. */
    public static @Nullable BlockPos find(ServerLevel level, int x, int nearY, int z, int radius, int vertical) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int r = 0; r <= radius; r++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dx = -r; dx <= r; dx++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                        continue;
                    }
                    for (int dy = 0; dy <= vertical; dy++) {
                        for (int sign = 0; sign < 2; sign++) {
                            int y = nearY + (sign == 0 ? dy : -dy - 1);
                            if (y <= level.getMinY() + 1 || y >= level.getMaxY() - 2) {
                                continue;
                            }
                            pos.set(x + dx, y, z + dz);
                            if (canStand(level, pos)) {
                                return pos.immutable();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Like {@link #find} but never fails: falls back to building a small platform. */
    public static BlockPos findOrBuild(ServerLevel level, int x, int nearY, int z, BlockState platform) {
        BlockPos found = find(level, x, nearY, z, 6, 24);
        if (found != null) {
            return found;
        }
        BlockPos feet = new BlockPos(x, Math.max(level.getMinY() + 2, Math.min(level.getMaxY() - 3, nearY)), z);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlockAndUpdate(feet.offset(dx, -1, dz), platform);
                level.setBlockAndUpdate(feet.offset(dx, 0, dz), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(feet.offset(dx, 1, dz), Blocks.AIR.defaultBlockState());
            }
        }
        return feet;
    }
}
