package com.eltavine.oneirgeo.world.gen;

import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.scene.Scene;
import com.eltavine.oneirgeo.world.gen.scene.SceneInfo;
import com.eltavine.oneirgeo.world.gen.scene.Scenes;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

/** Resolves (layer, region, scene) for positions of one dimension and one world seed. */
public final class LayoutSampler {
    private final DimensionLayout layout;
    private final long seed;
    private final DimensionLayout.Layer[] layers;
    private final Scene[][] scenes;
    private final int[][] cumulativeWeights;
    private final int[] originEntry;

    public record Region(int layerIndex, int entryIndex, int cellX, int cellZ, int centerX, int centerZ, double edgeDistance) {
    }

    public LayoutSampler(DimensionLayout layout, long seed) {
        this.layout = layout;
        this.seed = seed;
        List<DimensionLayout.Layer> list = layout.layers();
        this.layers = list.toArray(DimensionLayout.Layer[]::new);
        this.scenes = new Scene[this.layers.length][];
        this.cumulativeWeights = new int[this.layers.length][];
        this.originEntry = new int[this.layers.length];
        for (int l = 0; l < this.layers.length; l++) {
            List<DimensionLayout.SceneEntry> entries = this.layers[l].scenes();
            this.scenes[l] = new Scene[entries.size()];
            this.cumulativeWeights[l] = new int[entries.size()];
            int total = 0;
            this.originEntry[l] = -1;
            for (int e = 0; e < entries.size(); e++) {
                DimensionLayout.SceneEntry entry = entries.get(e);
                this.scenes[l][e] = Scenes.get(entry.scene());
                total += entry.weight();
                this.cumulativeWeights[l][e] = total;
                if (this.layers[l].originScene().filter(entry.scene()::equals).isPresent() && this.originEntry[l] < 0) {
                    this.originEntry[l] = e;
                }
            }
        }
    }

    public DimensionLayout layout() {
        return this.layout;
    }

    public long seed() {
        return this.seed;
    }

    public int layerCount() {
        return this.layers.length;
    }

    public DimensionLayout.Layer layer(int index) {
        return this.layers[index];
    }

    public Scene scene(int layerIndex, int entryIndex) {
        return this.scenes[layerIndex][entryIndex];
    }

    public DimensionLayout.SceneEntry entry(int layerIndex, int entryIndex) {
        return this.layers[layerIndex].scenes().get(entryIndex);
    }

    public int entryCount(int layerIndex) {
        return this.scenes[layerIndex].length;
    }

    /** Index of the layer containing {@code y}, or -1. */
    public int layerIndex(int y) {
        for (int i = 0; i < this.layers.length; i++) {
            if (this.layers[i].contains(y)) {
                return i;
            }
        }
        return -1;
    }

    /** Like {@link #layerIndex} but clamps to the closest layer, for biome lookups outside every layer. */
    public int nearestLayerIndex(int y) {
        int best = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < this.layers.length; i++) {
            DimensionLayout.Layer layer = this.layers[i];
            int distance = y < layer.minY() ? layer.minY() - y : (y > layer.maxY() ? y - layer.maxY() : 0);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        return best;
    }

    public long layerSeed(int layerIndex) {
        return Hash.of(this.seed, 0x51A7E, layerIndex);
    }

    public SceneInfo info(int layerIndex, Region region) {
        return new SceneInfo(this.layerSeed(layerIndex), this.layers[layerIndex], layerIndex, region);
    }

    public Region region(int layerIndex, int x, int z) {
        DimensionLayout.Layer layer = this.layers[layerIndex];
        int size = layer.regionSize();
        int cellX = Math.floorDiv(x, size);
        int cellZ = Math.floorDiv(z, size);
        long layerSeed = this.layerSeed(layerIndex);
        double best = Double.MAX_VALUE;
        double second = Double.MAX_VALUE;
        int bestX = 0;
        int bestZ = 0;
        int bestPointX = 0;
        int bestPointZ = 0;
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                int cx = cellX + dx;
                int cz = cellZ + dz;
                int px;
                int pz;
                if (cx == 0 && cz == 0) {
                    px = 0;
                    pz = 0;
                } else {
                    long h = Hash.of(layerSeed, cx, cz, 17);
                    px = cx * size + (int) (size * (0.15 + 0.7 * Hash.unit(h)));
                    pz = cz * size + (int) (size * (0.15 + 0.7 * Hash.unit(Hash.next(h, 1))));
                }
                double ddx = x - px;
                double ddz = z - pz;
                double d = Math.sqrt(ddx * ddx + ddz * ddz);
                if (d < best) {
                    second = best;
                    best = d;
                    bestX = cx;
                    bestZ = cz;
                    bestPointX = px;
                    bestPointZ = pz;
                } else if (d < second) {
                    second = d;
                }
            }
        }
        int entry = this.pickEntry(layerIndex, bestX, bestZ, layerSeed);
        return new Region(layerIndex, entry, bestX, bestZ, bestPointX, bestPointZ, (second - best) * 0.5);
    }

    private int pickEntry(int layerIndex, int cellX, int cellZ, long layerSeed) {
        int[] weights = this.cumulativeWeights[layerIndex];
        if (weights.length == 1) {
            return 0;
        }
        if (cellX == 0 && cellZ == 0 && this.originEntry[layerIndex] >= 0) {
            return this.originEntry[layerIndex];
        }
        int total = weights[weights.length - 1];
        int roll = Hash.range(Hash.of(layerSeed, cellX, cellZ, 99), 0, total - 1);
        for (int i = 0; i < weights.length; i++) {
            if (roll < weights[i]) {
                return i;
            }
        }
        return weights.length - 1;
    }

    public Holder<Biome> biome(int x, int y, int z) {
        int layerIndex = this.nearestLayerIndex(y);
        return this.biome(layerIndex, this.region(layerIndex, x, z), x, y, z);
    }

    public Holder<Biome> biome(int layerIndex, Region region, int x, int y, int z) {
        DimensionLayout.SceneEntry entry = this.entry(layerIndex, region.entryIndex());
        String variant = this.scenes[layerIndex][region.entryIndex()].biomeVariant(this.info(layerIndex, region), x, y, z);
        return entry.variant(variant);
    }

    /** Highest walkable surface over all layers at a column, or {@code Integer.MIN_VALUE}. */
    public int surfaceY(int x, int z) {
        for (int l = this.layers.length - 1; l >= 0; l--) {
            Region region = this.region(l, x, z);
            int y = this.scenes[l][region.entryIndex()].surfaceY(this.info(l, region), x, z);
            if (y != Integer.MIN_VALUE) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    /** Surface of the layer that contains {@code nearY}, falling back to {@link #surfaceY}. */
    public int surfaceYNear(int x, int nearY, int z) {
        int l = this.nearestLayerIndex(nearY);
        Region region = this.region(l, x, z);
        int y = this.scenes[l][region.entryIndex()].surfaceY(this.info(l, region), x, z);
        return y != Integer.MIN_VALUE ? y : this.surfaceY(x, z);
    }
}
