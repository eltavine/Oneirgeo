package com.eltavine.oneirgeo.world.gen.scene.end;

import com.eltavine.oneirgeo.util.Hash;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

/** Floating islands of the star cemetery: at most one per cell, plus the one under the arrival platform. */
public final class EndIslands {
    public static final int CELL = 88;
    public static final int SPAWN_X = 100;
    public static final int SPAWN_Z = 0;
    public static final int SPAWN_TOP = 48;
    private static final int SPAWN_RADIUS = 46;
    private static final int REACH = 56;

    public enum Kind {
        PLAIN,
        GRAVES,
        COLOSSUS,
        OBSERVATORY,
        CENTRAL
    }

    public record Island(int x, int z, int radius, int top, Kind kind, Direction facing, long hash) {
        /** 0 at the centre, 1 on the wobbly shore, above 1 outside. */
        public double t(int bx, int bz) {
            double dx = bx + 0.5 - this.x;
            double dz = bz + 0.5 - this.z;
            double phase = Hash.unit(this.hash) * Math.PI * 2;
            double angle = Math.atan2(dz, dx);
            double edge = this.radius * (1.0 + 0.11 * Math.sin(3 * angle + phase) + 0.06 * Math.sin(7 * angle + phase * 1.7));
            return Math.sqrt(dx * dx + dz * dz) / edge;
        }

        /** Walkable top at a column, or {@code Integer.MIN_VALUE} off the island. */
        public int topAt(int bx, int bz) {
            double t = this.t(bx, bz);
            if (t >= 1.0) {
                return Integer.MIN_VALUE;
            }
            return t > 0.9 ? this.top - 1 : this.top;
        }

        /** Lowest block of the island's hanging underside at a column. */
        public int bottomAt(int bx, int bz) {
            double t = this.t(bx, bz);
            double drip = Hash.unit(Hash.of(this.hash, bx >> 1, bz >> 1)) * 4.0;
            return this.top - (int) ((1.0 - t * t) * this.radius * 0.85 + drip) - 2;
        }
    }

    private EndIslands() {
    }

    public static @Nullable Island inCell(long seed, int cx, int cz) {
        long h = Hash.of(seed, cx, cz, 0x15);
        if (cx == Math.floorDiv(SPAWN_X, CELL) && cz == Math.floorDiv(SPAWN_Z, CELL)) {
            return new Island(SPAWN_X, SPAWN_Z, SPAWN_RADIUS, SPAWN_TOP, Kind.CENTRAL, Direction.WEST, h);
        }
        if (!Hash.chance(h, 0.62)) {
            return null;
        }
        boolean big = Hash.chance(Hash.next(h, 1), 0.24);
        int r = big ? Hash.range(Hash.next(h, 2), 28, 36) : Hash.range(Hash.next(h, 2), 9, 22);
        int x = cx * CELL + Hash.range(Hash.next(h, 3), r + 2, CELL - r - 2);
        int z = cz * CELL + Hash.range(Hash.next(h, 4), r + 2, CELL - r - 2);
        long dx = x - SPAWN_X;
        long dz = z - SPAWN_Z;
        long keep = (long) ((SPAWN_RADIUS + r) * 1.2) + 12;
        if (dx * dx + dz * dz < keep * keep) {
            return null;
        }
        int top = SPAWN_TOP + Hash.range(Hash.next(h, 5), -34, 42);
        double k = Hash.unit(Hash.next(h, 6));
        Kind kind;
        if (big) {
            kind = k < 0.42 ? Kind.OBSERVATORY : Kind.COLOSSUS;
        } else {
            kind = r >= 13 && k < 0.8 ? Kind.GRAVES : Kind.PLAIN;
        }
        Direction facing = Direction.from2DDataValue(Hash.range(Hash.next(h, 7), 0, 3));
        return new Island(x, z, r, top, kind, facing, h);
    }

    /** Every island whose outline may touch the given block rectangle (inclusive). */
    public static void forEachNear(long seed, int minX, int minZ, int maxX, int maxZ, Consumer<Island> action) {
        for (int cz = Math.floorDiv(minZ - REACH, CELL); cz <= Math.floorDiv(maxZ + REACH, CELL); cz++) {
            for (int cx = Math.floorDiv(minX - REACH, CELL); cx <= Math.floorDiv(maxX + REACH, CELL); cx++) {
                Island island = inCell(seed, cx, cz);
                if (island == null) {
                    continue;
                }
                int reach = (int) (island.radius() * 1.2) + 4;
                if (island.x() + reach >= minX && island.x() - reach <= maxX && island.z() + reach >= minZ && island.z() - reach <= maxZ) {
                    action.accept(island);
                }
            }
        }
    }

    public static int surfaceY(long seed, int x, int z) {
        int[] best = {Integer.MIN_VALUE};
        forEachNear(seed, x, z, x, z, island -> best[0] = Math.max(best[0], island.topAt(x, z)));
        return best[0];
    }
}
