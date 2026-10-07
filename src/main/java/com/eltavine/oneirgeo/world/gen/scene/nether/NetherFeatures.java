package com.eltavine.oneirgeo.world.gen.scene.nether;

import com.eltavine.oneirgeo.util.Hash;
import java.util.ArrayList;
import java.util.List;

/** Placement shared between the ash plains and the boiler corridors below them. */
public final class NetherFeatures {
    public static final int SINKHOLE_CELL = 180;
    public static final int CORRIDOR_TOP = -1301;

    private NetherFeatures() {
    }

    /** A ladder shaft from the ash down into the top floor of the boiler corridors. */
    public record Sinkhole(int x, int z) {
    }

    public static List<Sinkhole> sinkholesNear(long seed, int minX, int minZ, int maxX, int maxZ) {
        List<Sinkhole> found = new ArrayList<>();
        for (int cz = Math.floorDiv(minZ - 4, SINKHOLE_CELL); cz <= Math.floorDiv(maxZ + 4, SINKHOLE_CELL); cz++) {
            for (int cx = Math.floorDiv(minX - 4, SINKHOLE_CELL); cx <= Math.floorDiv(maxX + 4, SINKHOLE_CELL); cx++) {
                long h = Hash.of(seed, cx, cz, 0x5141);
                if (!Hash.chance(h, 0.45)) {
                    continue;
                }
                int x = cx * SINKHOLE_CELL + Hash.range(Hash.next(h, 1), 20, SINKHOLE_CELL - 20);
                int z = cz * SINKHOLE_CELL + Hash.range(Hash.next(h, 2), 20, SINKHOLE_CELL - 20);
                if (x + 3 >= minX && x - 3 <= maxX && z + 3 >= minZ && z - 3 <= maxZ) {
                    found.add(new Sinkhole(x, z));
                }
            }
        }
        return found;
    }
}
