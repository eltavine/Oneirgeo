package com.eltavine.oneirgeo.world.gen.landmark;

import com.eltavine.oneirgeo.space.ChunkSpaceData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Sparse grid of landmarks for one dimension. */
public interface LandmarkField {
    LandmarkField NONE = new LandmarkField() {
        @Override
        public int cellSize() {
            return 1 << 20;
        }

        @Override
        public int maxReach() {
            return 0;
        }

        @Override
        public void collectCell(long seed, int cellX, int cellZ, List<Landmark> out) {
        }

        @Override
        public @Nullable BlockState blockAt(Landmark landmark, long seed, int x, int y, int z) {
            return null;
        }
    };

    int cellSize();

    /** Largest horizontal distance a landmark may reach from its own cell. */
    int maxReach();

    void collectCell(long seed, int cellX, int cellZ, List<Landmark> out);

    /** Block of the landmark at a position, or null for none (including carved interiors). */
    @Nullable BlockState blockAt(Landmark landmark, long seed, int x, int y, int z);

    /** Spatial rules a landmark brings with it, such as a gravity shaft above a chimney. */
    default void collectSpace(Landmark landmark, long seed, ChunkSpaceData.Builder space) {
    }

    default List<Landmark> collect(long seed, int minX, int minZ, int maxX, int maxZ) {
        int size = this.cellSize();
        int reach = this.maxReach();
        int c0x = Math.floorDiv(minX - reach, size);
        int c1x = Math.floorDiv(maxX + reach, size);
        int c0z = Math.floorDiv(minZ - reach, size);
        int c1z = Math.floorDiv(maxZ + reach, size);
        List<Landmark> found = new ArrayList<>();
        List<Landmark> scratch = new ArrayList<>();
        for (int cz = c0z; cz <= c1z; cz++) {
            for (int cx = c0x; cx <= c1x; cx++) {
                scratch.clear();
                this.collectCell(seed, cx, cz, scratch);
                for (Landmark landmark : scratch) {
                    if (landmark.intersects(minX, minZ, maxX, maxZ)) {
                        found.add(landmark);
                    }
                }
            }
        }
        return found;
    }
}
