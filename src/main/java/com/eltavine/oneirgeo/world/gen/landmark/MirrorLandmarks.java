package com.eltavine.oneirgeo.world.gen.landmark;

import com.eltavine.oneirgeo.util.Hash;
import java.util.List;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Surreal still lifes over the mirror sea: a rock with a castle on it, door frames standing in the
 * water, a bowler hat, an apple. Every object is generated twice, once upside down under the surface.
 */
final class MirrorLandmarks implements LandmarkField {
    static final int CELL = 320;
    static final int MIRRORED = 0x40;

    static final int ROCK = 0;
    static final int DOOR = 1;
    static final int HAT = 2;
    static final int APPLE = 3;

    @Override
    public int cellSize() {
        return CELL;
    }

    @Override
    public int maxReach() {
        return 64;
    }

    @Override
    public void collectCell(long seed, int cellX, int cellZ, List<Landmark> out) {
        long h = Hash.of(seed, cellX, cellZ, 0x3177);
        if (!Hash.chance(h, 0.7) || (cellX == 0 && cellZ == 0)) {
            return;
        }
        int x = cellX * CELL + Hash.range(Hash.next(h, 1), 64, CELL - 64);
        int z = cellZ * CELL + Hash.range(Hash.next(h, 2), 64, CELL - 64);
        int kind = Hash.range(Hash.next(h, 3), 0, 3);
        Landmark original = switch (kind) {
            case ROCK -> {
                int r = Hash.range(Hash.next(h, 4), 14, 34);
                int y = Hash.range(Hash.next(h, 5), 70, 260);
                yield new Landmark(Landmark.Shape.SPHERE, x, y, z, r, (int) (r * 1.6) + r / 2 + 12, r, 0xFF8C9097, ROCK);
            }
            case DOOR -> {
                int height = Hash.range(Hash.next(h, 4), 90, 160);
                yield new Landmark(Landmark.Shape.BOX, x, 1, z, height / 3, height, 1, 0xFFF3F1EC, DOOR);
            }
            case HAT -> {
                int y = Hash.range(Hash.next(h, 5), 40, 200);
                yield new Landmark(Landmark.Shape.CYLINDER, x, y, z, 17, 14, 17, 0xFF141416, HAT);
            }
            default -> {
                int r = Hash.range(Hash.next(h, 4), 12, 22);
                int y = Hash.range(Hash.next(h, 5), 30, 180);
                yield new Landmark(Landmark.Shape.SPHERE, x, y, z, r, 2 * r + 8, r, 0xFF5E9E3B, APPLE);
            }
        };
        out.add(original);
        out.add(new Landmark(original.shape(), x, -original.maxY(), z, original.radiusX(), original.height(), original.radiusZ(),
                original.color(), original.style() | MIRRORED));
    }

    @Override
    public @Nullable BlockState blockAt(Landmark l, long seed, int x, int y, int z) {
        boolean mirrored = (l.style() & MIRRORED) != 0;
        int baseY = mirrored ? -l.maxY() : l.y();
        int top = baseY + l.height() - 1;
        int ly = mirrored ? top - (y - l.y()) : y;
        if (ly < baseY || ly > top) {
            return null;
        }
        int u = ly - baseY;
        double dx = x + 0.5 - l.x();
        double dz = z + 0.5 - l.z();
        double d = Math.sqrt(dx * dx + dz * dz);
        return switch (l.style() & ~MIRRORED) {
            case ROCK -> rock(l, seed, x, z, u, dx, dz, d);
            case DOOR -> {
                boolean frame = Math.abs(dx) > l.radiusX() - 3 || u > l.height() - 4;
                yield frame && Math.abs(dz) <= 1.0 && Math.abs(dx) <= l.radiusX() ? Blocks.CONCRETE.pick(DyeColor.WHITE).defaultBlockState() : null;
            }
            case HAT -> {
                if (u < 2) {
                    yield d <= 17 ? Blocks.WOOL.pick(DyeColor.BLACK).defaultBlockState() : null;
                }
                if (d <= 10) {
                    yield u >= 2 && u <= 3 ? Blocks.WOOL.pick(DyeColor.GRAY).defaultBlockState() : Blocks.WOOL.pick(DyeColor.BLACK).defaultBlockState();
                }
                yield null;
            }
            default -> apple(l, u, dx, dz, d);
        };
    }

    private static @Nullable BlockState rock(Landmark l, long seed, int x, int z, int u, double dx, double dz, double d) {
        int r = l.radiusX();
        int sphereHeight = (int) (r * 1.6);
        if (u < sphereHeight) {
            double ny = (u + 0.5) / sphereHeight * 2.0 - 1.0;
            double jag = 1.0 + 0.18 * (Hash.unit(Hash.of(seed, x >> 2, z >> 2, u >> 3)) - 0.5);
            double nr = d / (r * jag);
            if (ny > 0.55) {
                return nr * nr + ny * ny <= 1.0 && ny < 0.62 ? Blocks.GRASS_BLOCK.defaultBlockState()
                        : (nr < 0.75 && ny <= 0.62 ? Blocks.STONE.defaultBlockState() : null);
            }
            return nr * nr + ny * ny <= 1.0 ? (u % 7 == 0 ? Blocks.ANDESITE.defaultBlockState() : Blocks.STONE.defaultBlockState()) : null;
        }
        int towerBase = (int) (sphereHeight * 0.81);
        if (u >= towerBase && d < r * 0.45) {
            double angle = Math.atan2(dz, dx);
            int towerIndex = (int) Math.floor((angle + Math.PI) / (Math.PI / 2));
            double tx = Math.cos(towerIndex * Math.PI / 2 + Math.PI / 4 - Math.PI) * r * 0.3;
            double tz = Math.sin(towerIndex * Math.PI / 2 + Math.PI / 4 - Math.PI) * r * 0.3;
            double td = Math.hypot(dx - tx, dz - tz);
            int towerTop = towerBase + 8 + (towerIndex % 2) * 5;
            if (td <= 2.5 && u <= towerTop) {
                return u == towerTop ? Blocks.STONE_BRICK_WALL.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
            }
            if (d < r * 0.22 && u <= towerBase + 5) {
                return Blocks.STONE_BRICKS.defaultBlockState();
            }
        }
        return null;
    }

    private static @Nullable BlockState apple(Landmark l, int u, double dx, double dz, double d) {
        int r = l.radiusX();
        int body = 2 * r;
        if (u < body) {
            double ny = (u + 0.5 - r) / r;
            double dimple = ny > 0.75 ? (ny - 0.75) * 1.6 : (ny < -0.85 ? (-0.85 - ny) * 1.2 : 0.0);
            double nr = d / r + dimple;
            return nr * nr + ny * ny <= 1.0 ? Blocks.CONCRETE.pick(DyeColor.LIME).defaultBlockState() : null;
        }
        if (d < 1.2 && u < body + 6) {
            return Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
        }
        if (u >= body + 3 && u <= body + 4 && dx > 0.5 && dx < 6.0 && Math.abs(dz) < 2.0) {
            return Blocks.AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        }
        return null;
    }
}
