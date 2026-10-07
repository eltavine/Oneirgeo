package com.eltavine.oneirgeo.world.gen;

import com.eltavine.oneirgeo.util.Hash;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

/**
 * Biomes follow the layout exactly: one biome per scene (plus optional variants such as closed rooms),
 * assigned in 3D so every height layer can carry its own fog, sky and spawn rules.
 */
public final class OneirgeoBiomeSource extends BiomeSource {
    public static final MapCodec<OneirgeoBiomeSource> CODEC = DimensionLayout.CODEC.fieldOf("layout")
            .xmap(OneirgeoBiomeSource::new, OneirgeoBiomeSource::layoutHolder).stable();

    private final Holder<DimensionLayout> layout;
    private volatile LayoutSampler sampler;
    private volatile long worldSeed = Long.MIN_VALUE;

    public OneirgeoBiomeSource(Holder<DimensionLayout> layout) {
        this.layout = layout;
    }

    public Holder<DimensionLayout> layoutHolder() {
        return this.layout;
    }

    private long salt() {
        return this.layout.unwrapKey().map(key -> Hash.salt(key.identifier().toString())).orElse(0x0E1A6E0L);
    }

    /** Binds the world seed; called by the generator as soon as a {@code RandomState} is available. */
    public void initSeed(long seed) {
        if (this.sampler != null && this.worldSeed == seed) {
            return;
        }
        synchronized (this) {
            if (this.sampler == null || this.worldSeed != seed) {
                this.sampler = new LayoutSampler(this.layout.value(), Hash.of(seed, this.salt()));
                this.worldSeed = seed;
            }
        }
    }

    public LayoutSampler sampler() {
        LayoutSampler current = this.sampler;
        if (current == null) {
            this.initSeed(0L);
            current = this.sampler;
        }
        return current;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.layout.value().allBiomes();
    }

    @Override
    public BiomeResolver createResolver(Climate.Sampler climateSampler) {
        return (quartX, quartY, quartZ) -> this.sampler().biome((quartX << 2) + 2, (quartY << 2) + 2, (quartZ << 2) + 2);
    }

    /** Regions only depend on (layer, x, z), so one chunk needs at most layers * 16 region lookups. */
    @Override
    public BiomeResolver createResolverForChunk(Climate.Sampler climateSampler, int minQuartX, int minQuartY, int minQuartZ,
                                                int quartSizeX, int quartSizeY, int quartSizeZ) {
        LayoutSampler sampler = this.sampler();
        LayoutSampler.Region[][] cache = new LayoutSampler.Region[sampler.layerCount()][quartSizeX * quartSizeZ];
        return (quartX, quartY, quartZ) -> {
            int x = (quartX << 2) + 2;
            int y = (quartY << 2) + 2;
            int z = (quartZ << 2) + 2;
            int lx = quartX - minQuartX;
            int lz = quartZ - minQuartZ;
            int layer = sampler.nearestLayerIndex(y);
            if (lx < 0 || lx >= quartSizeX || lz < 0 || lz >= quartSizeZ) {
                return sampler.biome(x, y, z);
            }
            int index = lx + lz * quartSizeX;
            LayoutSampler.Region region = cache[layer][index];
            if (region == null) {
                region = sampler.region(layer, x, z);
                cache[layer][index] = region;
            }
            return sampler.biome(layer, region, x, y, z);
        };
    }
}
