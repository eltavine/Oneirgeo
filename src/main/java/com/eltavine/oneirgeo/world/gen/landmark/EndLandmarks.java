package com.eltavine.oneirgeo.world.gen.landmark;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.util.Hash;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Obelisks floating far above the cemetery, luminous halos that circle nothing, and spiral stairs
 * that climb from the graves into the geometry of the void.
 */
final class EndLandmarks implements LandmarkField {
    static final int CELL = 512;
    static final int OBELISK = 0;
    static final int HALO = 1;
    static final int STAIR = 2;
    private static final int STAIR_BASE = 40;
    private static final int STAIR_TOP = 900;
    private static final int STAIR_TURN = 32;

    @Override
    public int cellSize() {
        return CELL;
    }

    @Override
    public int maxReach() {
        return 200;
    }

    @Override
    public void collectCell(long seed, int cellX, int cellZ, List<Landmark> out) {
        long h = Hash.of(seed, cellX, cellZ, 0xE4D);
        double kind = Hash.unit(h);
        int x = cellX * CELL + Hash.range(Hash.next(h, 1), 120, CELL - 120);
        int z = cellZ * CELL + Hash.range(Hash.next(h, 2), 120, CELL - 120);
        if (cellX == 0 && cellZ == 0) {
            stair(420, 60, out);
            return;
        }
        long dx = x - 100L;
        if (dx * dx + (long) z * z < 260L * 260L) {
            return;
        }
        if (kind < 0.42) {
            int r = Hash.range(Hash.next(h, 3), 6, 13);
            int height = Hash.range(Hash.next(h, 4), 220, 560);
            int y = Hash.range(Hash.next(h, 5), 880, 1440);
            out.add(new Landmark(Landmark.Shape.BOX, x, y, z, r, height, r, 0xFFE9E6F0, OBELISK));
        } else if (kind < 0.66) {
            int radius = Hash.range(Hash.next(h, 3), 80, 170);
            int tube = Hash.range(Hash.next(h, 4), 3, 7);
            int y = Hash.range(Hash.next(h, 5), 980, 1700);
            out.add(new Landmark(Landmark.Shape.RING, x, y, z, radius, tube * 2 + 1, tube, 0xFFF4EED8, HALO));
        } else if (kind < 0.78) {
            stair(x, z, out);
        }
    }

    private static void stair(int x, int z, List<Landmark> out) {
        out.add(new Landmark(Landmark.Shape.CYLINDER, x, STAIR_BASE, z, 10, STAIR_TOP - STAIR_BASE + 1, 10, 0xFFD9CCE8, STAIR));
    }

    @Override
    public @Nullable BlockState blockAt(Landmark l, long seed, int x, int y, int z) {
        if (!l.contains(x, y, z)) {
            return null;
        }
        if (l.style() == OBELISK) {
            return Math.floorMod(y - l.y(), 40) < 2 ? Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState() : OneirgeoBlocks.STAR_STONE.defaultBlockState();
        }
        if (l.style() == STAIR) {
            return stairBlock(l, x, y, z);
        }
        long h = Hash.of(seed, x >> 3, y >> 3, z >> 3);
        return Hash.chance(h, 0.6) ? Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState() : Blocks.END_STONE_BRICKS.defaultBlockState();
    }

    /** A purpur core with a ring of steps around it; a broad disc at the foot and a deck at the head. */
    private static @Nullable BlockState stairBlock(Landmark l, int x, int y, int z) {
        double dx = x + 0.5 - l.x();
        double dz = z + 0.5 - l.z();
        double d = Math.sqrt(dx * dx + dz * dz);
        int rel = y - l.y();
        if (d < 1.6) {
            return rel % 12 == 0 ? Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState() : Blocks.PURPUR_PILLAR.defaultBlockState();
        }
        if (d > 10.0) {
            return null;
        }
        if (rel <= 2 || (y >= l.maxY() - 1 && d >= 5.6)) {
            return Blocks.END_STONE_BRICKS.defaultBlockState();
        }
        if (d <= 5.6 && y <= l.maxY() - 1) {
            double angle = Math.atan2(dz, dx);
            if (angle < 0) {
                angle += Math.PI * 2;
            }
            double step = Math.PI * 2 / STAIR_TURN;
            double diff = angle - Math.floorMod(rel, STAIR_TURN) * step;
            if (diff < 0) {
                diff += Math.PI * 2;
            }
            if (diff < step * 1.6) {
                return Blocks.END_STONE_BRICKS.defaultBlockState();
            }
        }
        return null;
    }
}
