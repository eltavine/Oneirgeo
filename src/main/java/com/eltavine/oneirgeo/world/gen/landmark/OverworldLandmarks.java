package com.eltavine.oneirgeo.world.gen.landmark;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.util.Hash;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Monoliths rising out of the plains, marble pillars that hold up the cloud sea (with a stair spiral
 * wound around them) and updraft wells: the only two ways up besides climbing.
 */
final class OverworldLandmarks implements LandmarkField {
    static final int CELL = 448;
    static final int GROUND = 64;
    static final int CLOUD_FLOOR = 1700;
    static final int SPAWN_CLEARANCE = 220;

    static final int MONOLITH = 0;
    static final int PILLAR = 1;
    static final int SPIRE = 2;
    static final int WELL = 3;

    private static final int STAIR_TURN = 96;

    @Override
    public int cellSize() {
        return CELL;
    }

    @Override
    public int maxReach() {
        return 96;
    }

    @Override
    public void collectCell(long seed, int cellX, int cellZ, List<Landmark> out) {
        long h = Hash.of(seed, cellX, cellZ, 0x1A4D);
        if (!Hash.chance(h, 0.62)) {
            return;
        }
        int x = cellX * CELL + Hash.range(Hash.next(h, 1), 96, CELL - 96);
        int z = cellZ * CELL + Hash.range(Hash.next(h, 2), 96, CELL - 96);
        if ((long) x * x + (long) z * z < (long) SPAWN_CLEARANCE * SPAWN_CLEARANCE) {
            return;
        }
        double kind = Hash.unit(Hash.next(h, 3));
        if (kind < 0.38) {
            boolean slab = Hash.chance(Hash.next(h, 4), 0.35);
            int rx = slab ? Hash.range(Hash.next(h, 5), 3, 6) : Hash.range(Hash.next(h, 5), 9, 20);
            int rz = slab ? Hash.range(Hash.next(h, 6), 16, 30) : Hash.range(Hash.next(h, 6), 9, 20);
            if (Hash.chance(Hash.next(h, 7), 0.5)) {
                int t = rx;
                rx = rz;
                rz = t;
            }
            int height = Hash.range(Hash.next(h, 8), 380, 1500);
            out.add(new Landmark(Landmark.Shape.BOX, x, GROUND - 24, z, rx, height, rz, 0xFF26262C, MONOLITH));
        } else if (kind < 0.66) {
            int r = Hash.range(Hash.next(h, 5), 11, 19);
            out.add(new Landmark(Landmark.Shape.CYLINDER, x, GROUND - 40, z, r, CLOUD_FLOOR - GROUND + 48, r, 0xFFE6E1D6, PILLAR));
        } else if (kind < 0.82) {
            int r = Hash.range(Hash.next(h, 5), 28, 56);
            int height = Hash.range(Hash.next(h, 8), 520, 1150);
            out.add(new Landmark(Landmark.Shape.SPIRE, x, GROUND - 20, z, r, height, r, 0xFF3C3B46, SPIRE));
        } else {
            out.add(new Landmark(Landmark.Shape.CYLINDER, x, GROUND - 2, z, 4, CLOUD_FLOOR - GROUND + 6, 4, 0x40FFFFFF, WELL));
        }
    }

    @Override
    public @Nullable BlockState blockAt(Landmark l, long seed, int x, int y, int z) {
        switch (l.style()) {
            case MONOLITH -> {
                if (!l.contains(x, y, z)) {
                    return null;
                }
                boolean seamLine = Math.floorMod(y - l.y(), 64) == 0;
                return seamLine ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : OneirgeoBlocks.MONOLITH_STONE.defaultBlockState();
            }
            case PILLAR -> {
                double dx = x + 0.5 - l.x();
                double dz = z + 0.5 - l.z();
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d <= l.radiusX()) {
                    if (y < l.y() || y > l.maxY()) {
                        return null;
                    }
                    boolean fluted = d > l.radiusX() - 1.2 && Math.floorMod((int) Math.floor(Math.atan2(dz, dx) * 12 / Math.PI), 2) == 0;
                    return fluted ? Blocks.SMOOTH_QUARTZ.defaultBlockState() : OneirgeoBlocks.TEMPLE_MARBLE.defaultBlockState();
                }
                if (d < l.radiusX() + 4.0 && y >= GROUND && y < CLOUD_FLOOR + 6) {
                    if (stairStep(l, dx, dz, y)) {
                        return OneirgeoBlocks.TEMPLE_MARBLE.defaultBlockState();
                    }
                }
                if (d < l.radiusX() + 4.5 && y >= CLOUD_FLOOR - 24 && y < CLOUD_FLOOR + 24) {
                    return Blocks.AIR.defaultBlockState();
                }
                return null;
            }
            case SPIRE -> {
                if (!l.contains(x, y, z)) {
                    return null;
                }
                return Math.floorMod(y, 9) == 0 ? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState();
            }
            case WELL -> {
                double dx = x + 0.5 - l.x();
                double dz = z + 0.5 - l.z();
                double d2 = dx * dx + dz * dz;
                if (y <= GROUND + 1) {
                    if (d2 <= 2.3 * 2.3) {
                        return y == GROUND + 1 ? OneirgeoBlocks.UPDRAFT_VENT.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState();
                    }
                    if (d2 <= 4.6 * 4.6 && y >= GROUND - 1) {
                        return y == GROUND + 1 ? Blocks.SMOOTH_STONE_SLAB.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState();
                    }
                    return null;
                }
                if (d2 <= 2.3 * 2.3 && y < CLOUD_FLOOR + 2) {
                    return OneirgeoBlocks.UPDRAFT.defaultBlockState();
                }
                return null;
            }
            default -> {
                return null;
            }
        }
    }

    private static boolean stairStep(Landmark l, double dx, double dz, int y) {
        double angle = Math.atan2(dz, dx);
        if (angle < 0) {
            angle += Math.PI * 2;
        }
        double target = Math.floorMod(y - GROUND, STAIR_TURN) * (Math.PI * 2 / STAIR_TURN);
        double diff = angle - target;
        if (diff < 0) {
            diff += Math.PI * 2;
        }
        return diff < (Math.PI * 2 / STAIR_TURN) * 1.6;
    }
}
