package com.eltavine.oneirgeo.client.datagen;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.world.OneirgeoBiomes;
import com.eltavine.oneirgeo.world.OneirgeoDimensions;
import com.eltavine.oneirgeo.world.gen.DimensionLayout;
import com.eltavine.oneirgeo.world.gen.OneirgeoChunkGenerator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

/** The six dimensions as layer stacks, plus the level stems and the replacement default world preset. */
final class LayoutData {
    private static final int MIN = OneirgeoDimensions.MIN_Y;
    private static final int MAX = OneirgeoDimensions.MAX_Y;

    private LayoutData() {
    }

    private static DimensionLayout.SceneEntry scene(HolderGetter<Biome> biomes, String scene, ResourceKey<Biome> biome, int weight) {
        return new DimensionLayout.SceneEntry(Oneirgeo.id(scene), biomes.getOrThrow(biome), weight, Map.of());
    }

    private static DimensionLayout.SceneEntry closedScene(HolderGetter<Biome> biomes, String scene, ResourceKey<Biome> biome) {
        return new DimensionLayout.SceneEntry(Oneirgeo.id(scene), biomes.getOrThrow(biome), 1,
                Map.of("closed", biomes.getOrThrow(OneirgeoBiomes.CLOSED_ROOM)));
    }

    private static DimensionLayout.Layer layer(String name, int minY, int maxY, int regionSize, List<DimensionLayout.SceneEntry> scenes) {
        return new DimensionLayout.Layer(name, minY, maxY, regionSize, scenes, Optional.empty());
    }

    static void bootstrap(BootstrapContext<DimensionLayout> context) {
        HolderGetter<Biome> b = context.lookup(Registries.BIOME);

        context.register(OneirgeoDimensions.OVERWORLD_LAYOUT, new DimensionLayout(List.of(
                layer("web", MIN, MAX, 1024, List.of(scene(b, "neural_web", OneirgeoBiomes.NEURAL_FOG, 1)))
        ), Oneirgeo.id("overworld"), 63, 65));

        context.register(OneirgeoDimensions.NETHER_LAYOUT, new DimensionLayout(List.of(
                layer("depths", MIN, -1301, 512, List.of(closedScene(b, "boiler_corridors", OneirgeoBiomes.BOILER_CORRIDORS))),
                new DimensionLayout.Layer("furnace", -1300, -601, 560, List.of(
                        scene(b, "ash_plains", OneirgeoBiomes.ASH_PLAINS, 3),
                        scene(b, "lava_sea", OneirgeoBiomes.LAVA_SEA, 1)), Optional.of(Oneirgeo.id("ash_plains"))),
                layer("hearth", -600, 1399, 1024, List.of(scene(b, "great_hearth", OneirgeoBiomes.GREAT_HEARTH, 1))),
                layer("city", 1400, MAX, 1024, List.of(scene(b, "hanging_city", OneirgeoBiomes.HANGING_CITY, 1)))
        ), Oneirgeo.id("nether"), -1214, -1209));

        context.register(OneirgeoDimensions.END_LAYOUT, new DimensionLayout(List.of(
                layer("sea", MIN, -401, 1024, List.of(scene(b, "night_sea", OneirgeoBiomes.NIGHT_SEA, 1))),
                layer("cemetery", -400, 799, 1024, List.of(scene(b, "star_cemetery", OneirgeoBiomes.STAR_CEMETERY, 1))),
                layer("geometry", 800, MAX, 1024, List.of(scene(b, "void_geometry", OneirgeoBiomes.VOID_GEOMETRY, 1)))
        ), Oneirgeo.id("end"), -1000, 49));

        context.register(OneirgeoDimensions.MIRROR_SEA_LAYOUT, new DimensionLayout(List.of(
                layer("mirror", MIN, MAX, 1024, List.of(scene(b, "mirror_sea", OneirgeoBiomes.MIRROR_SEA, 1)))
        ), Oneirgeo.id("mirror_sea"), 0, 1));

        context.register(OneirgeoDimensions.POOLROOMS_LAYOUT, new DimensionLayout(List.of(
                layer("pools", MIN, MAX, 1024, List.of(closedScene(b, "poolrooms", OneirgeoBiomes.POOLROOMS)))
        ), Oneirgeo.id("none"), 0, 1));

        context.register(OneirgeoDimensions.BACKROOMS_LAYOUT, new DimensionLayout(List.of(
                layer("offices", MIN, MAX, 1024, List.of(closedScene(b, "backrooms", OneirgeoBiomes.BACKROOMS)))
        ), Oneirgeo.id("none"), 0, 1));
    }

    private static LevelStem stem(HolderGetter<DimensionType> types, HolderGetter<DimensionLayout> layouts,
                                  ResourceKey<DimensionType> type, ResourceKey<DimensionLayout> layout) {
        return new LevelStem(types.getOrThrow(type), new OneirgeoChunkGenerator(layouts.getOrThrow(layout)));
    }

    static void stems(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> types = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<DimensionLayout> layouts = context.lookup(DimensionLayout.REGISTRY_KEY);
        context.register(OneirgeoDimensions.MIRROR_SEA_STEM, stem(types, layouts, OneirgeoDimensions.MIRROR_SEA_TYPE, OneirgeoDimensions.MIRROR_SEA_LAYOUT));
        context.register(OneirgeoDimensions.POOLROOMS_STEM, stem(types, layouts, OneirgeoDimensions.POOLROOMS_TYPE, OneirgeoDimensions.POOLROOMS_LAYOUT));
        context.register(OneirgeoDimensions.BACKROOMS_STEM, stem(types, layouts, OneirgeoDimensions.BACKROOMS_TYPE, OneirgeoDimensions.BACKROOMS_LAYOUT));
    }

    static WorldPreset normalPreset(HolderGetter<DimensionType> types, HolderGetter<DimensionLayout> layouts) {
        return new WorldPreset(Map.of(
                LevelStem.OVERWORLD, stem(types, layouts, OneirgeoDimensions.OVERWORLD_TYPE, OneirgeoDimensions.OVERWORLD_LAYOUT),
                LevelStem.NETHER, stem(types, layouts, OneirgeoDimensions.NETHER_TYPE, OneirgeoDimensions.NETHER_LAYOUT),
                LevelStem.END, stem(types, layouts, OneirgeoDimensions.END_TYPE, OneirgeoDimensions.END_LAYOUT)));
    }
}
