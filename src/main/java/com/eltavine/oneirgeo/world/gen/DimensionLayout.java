package com.eltavine.oneirgeo.world.gen;

import com.eltavine.oneirgeo.Oneirgeo;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Describes one dimension as a stack of height layers. Each layer is partitioned horizontally into
 * regions (jittered Voronoi cells), and every region is assigned one scene.
 */
public record DimensionLayout(List<Layer> layers, Identifier landmarks, int seaLevel, int spawnY) {
    public static final ResourceKey<Registry<DimensionLayout>> REGISTRY_KEY = ResourceKey.createRegistryKey(Oneirgeo.id("dimension_layout"));

    public static final Codec<DimensionLayout> DIRECT_CODEC = RecordCodecBuilder.<DimensionLayout>create(i -> i.group(
            Layer.CODEC.listOf().fieldOf("layers").forGetter(DimensionLayout::layers),
            Identifier.CODEC.optionalFieldOf("landmarks", Oneirgeo.id("none")).forGetter(DimensionLayout::landmarks),
            Codec.INT.fieldOf("sea_level").forGetter(DimensionLayout::seaLevel),
            Codec.INT.fieldOf("spawn_y").forGetter(DimensionLayout::spawnY)
    ).apply(i, DimensionLayout::new)).validate(DimensionLayout::validate);

    public static final Codec<Holder<DimensionLayout>> CODEC = RegistryCodecs.holder(REGISTRY_KEY, DIRECT_CODEC);

    private static DataResult<DimensionLayout> validate(DimensionLayout layout) {
        List<Layer> layers = layout.layers();
        if (layers.isEmpty()) {
            return DataResult.error(() -> "A dimension layout needs at least one layer");
        }
        for (int i = 1; i < layers.size(); i++) {
            if (layers.get(i).minY() <= layers.get(i - 1).maxY()) {
                return DataResult.error(() -> "Layers must be sorted bottom to top and must not overlap");
            }
        }
        for (Layer layer : layers) {
            if (layer.scenes().isEmpty()) {
                return DataResult.error(() -> "Layer " + layer.name() + " has no scenes");
            }
        }
        return DataResult.success(layout);
    }

    public Stream<Holder<Biome>> allBiomes() {
        return this.layers.stream().flatMap(layer -> layer.scenes().stream()).flatMap(SceneEntry::allBiomes);
    }

    public record Layer(String name, int minY, int maxY, int regionSize, List<SceneEntry> scenes, Optional<Identifier> originScene) {
        public static final Codec<Layer> CODEC = RecordCodecBuilder.<Layer>create(i -> i.group(
                Codec.STRING.fieldOf("name").forGetter(Layer::name),
                Codec.INT.fieldOf("min_y").forGetter(Layer::minY),
                Codec.INT.fieldOf("max_y").forGetter(Layer::maxY),
                Codec.intRange(16, 1 << 16).optionalFieldOf("region_size", 512).forGetter(Layer::regionSize),
                SceneEntry.CODEC.listOf().fieldOf("scenes").forGetter(Layer::scenes),
                Identifier.CODEC.optionalFieldOf("origin_scene").forGetter(Layer::originScene)
        ).apply(i, Layer::new)).validate(layer -> layer.maxY() < layer.minY()
                ? DataResult.error(() -> "Layer " + layer.name() + " has max_y below min_y")
                : DataResult.success(layer));

        public boolean contains(int y) {
            return y >= this.minY && y <= this.maxY;
        }
    }

    public record SceneEntry(Identifier scene, Holder<Biome> biome, int weight, Map<String, Holder<Biome>> variants) {
        public static final Codec<SceneEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("scene").forGetter(SceneEntry::scene),
                Biome.CODEC.fieldOf("biome").forGetter(SceneEntry::biome),
                Codec.intRange(1, 1 << 16).optionalFieldOf("weight", 1).forGetter(SceneEntry::weight),
                Codec.unboundedMap(Codec.STRING, Biome.CODEC).optionalFieldOf("variants", Map.of()).forGetter(SceneEntry::variants)
        ).apply(i, SceneEntry::new));

        public Holder<Biome> variant(String name) {
            return name == null ? this.biome : this.variants.getOrDefault(name, this.biome);
        }

        public Stream<Holder<Biome>> allBiomes() {
            return Stream.concat(Stream.of(this.biome), this.variants.values().stream());
        }
    }
}
