package com.eltavine.oneirgeo.world.gen.landmark;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.space.Box;
import com.eltavine.oneirgeo.space.ChunkSpaceData;
import com.eltavine.oneirgeo.util.Hash;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Spires hanging from the ceiling of the world, and the chimneys of a furnace that went out long ago.
 * Hot air still rises through every chimney; above its mouth gravity turns over, and the shaft lets
 * you fall upwards all the way to the hanging city.
 */
final class NetherLandmarks implements LandmarkField {
    static final int CELL = 384;
    static final int CEILING = 1904;
    static final int ASH_GROUND = -1210;
    static final int STALACTITE = 0;
    static final int CHIMNEY = 1;
    /** The open column above a chimney mouth; never drawn as a silhouette. */
    static final int SHAFT = 2;

    @Override
    public int cellSize() {
        return CELL;
    }

    @Override
    public int maxReach() {
        return 80;
    }

    @Override
    public void collectCell(long seed, int cellX, int cellZ, List<Landmark> out) {
        long h = Hash.of(seed, cellX, cellZ, 0x4E7);
        double kind = Hash.unit(h);
        int x = cellX * CELL + Hash.range(Hash.next(h, 1), 80, CELL - 80);
        int z = cellZ * CELL + Hash.range(Hash.next(h, 2), 80, CELL - 80);
        if (cellX == 0 && cellZ == 0) {
            chimney(48, 40, 10, 900, out);
            return;
        }
        if (kind < 0.5) {
            int height = Hash.range(Hash.next(h, 3), 320, 1150);
            int r = Hash.range(Hash.next(h, 4), 18, 58);
            out.add(new Landmark(Landmark.Shape.STALACTITE, x, CEILING - height, z, r, height, r, 0xFF2B1B1E, STALACTITE));
        } else if (kind < 0.8) {
            chimney(x, z, Hash.range(Hash.next(h, 4), 8, 14), Hash.range(Hash.next(h, 3), 520, 1500), out);
        }
    }

    private static void chimney(int x, int z, int r, int height, List<Landmark> out) {
        Landmark chimney = new Landmark(Landmark.Shape.CYLINDER, x, ASH_GROUND - 12, z, r, height, r, 0xFF3A1F1C, CHIMNEY);
        out.add(chimney);
        int inner = r - 3;
        int bottom = chimney.maxY() - 5;
        out.add(new Landmark(Landmark.Shape.CYLINDER, x, bottom, z, inner + 1, CEILING - bottom, inner + 1, 0, SHAFT));
    }

    @Override
    public @Nullable BlockState blockAt(Landmark l, long seed, int x, int y, int z) {
        if (!l.contains(x, y, z)) {
            return null;
        }
        if (l.style() == SHAFT) {
            return Blocks.AIR.defaultBlockState();
        }
        if (l.style() == STALACTITE) {
            int band = Math.floorMod(y, 23);
            return band < 2 ? Blocks.MAGMA_BLOCK.defaultBlockState() : (band < 12 ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState());
        }
        double dx = x + 0.5 - l.x();
        double dz = z + 0.5 - l.z();
        double d = Math.sqrt(dx * dx + dz * dz);
        boolean doorway = y > ASH_GROUND && y <= ASH_GROUND + 5 && dx > 0 && Math.abs(dz) <= 1.5;
        if (d < l.radiusX() - 3.0 || doorway) {
            if (y <= ASH_GROUND || y > l.maxY() - 6) {
                return d < l.radiusX() - 3.0 && y <= ASH_GROUND ? Blocks.BLACKSTONE.defaultBlockState() : null;
            }
            return doorway && d >= l.radiusX() - 3.0 ? Blocks.AIR.defaultBlockState() : OneirgeoBlocks.UPDRAFT.defaultBlockState();
        }
        if (y > l.maxY() - 10) {
            return OneirgeoBlocks.EMBER.defaultBlockState();
        }
        return Math.floorMod(y, 31) == 0 ? OneirgeoBlocks.BOILER_PLATE.defaultBlockState() : Blocks.NETHER_BRICKS.defaultBlockState();
    }

    @Override
    public void collectSpace(Landmark l, long seed, ChunkSpaceData.Builder space) {
        if (l.style() != CHIMNEY) {
            return;
        }
        int inner = l.radiusX() - 3;
        space.flip(new Box(l.x() - inner, l.maxY() - 5, l.z() - inner, l.x() + inner + 1, CEILING, l.z() + inner + 1));
    }
}
