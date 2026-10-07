package com.eltavine.oneirgeo.world;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.world.gen.DimensionLayout;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public final class OneirgeoDimensions {
    /** Every dimension shares the full vanilla range: Y -2032 to 2031. */
    public static final int MIN_Y = DimensionType.MIN_Y;
    public static final int HEIGHT = DimensionType.Y_SIZE;
    public static final int MAX_Y = MIN_Y + HEIGHT - 1;

    public static final ResourceKey<DimensionType> OVERWORLD_TYPE = type("overworld");
    public static final ResourceKey<DimensionType> NETHER_TYPE = type("the_nether");
    public static final ResourceKey<DimensionType> END_TYPE = type("the_end");
    public static final ResourceKey<DimensionType> MIRROR_SEA_TYPE = type("mirror_sea");
    public static final ResourceKey<DimensionType> POOLROOMS_TYPE = type("poolrooms");
    public static final ResourceKey<DimensionType> BACKROOMS_TYPE = type("backrooms");

    public static final ResourceKey<DimensionLayout> OVERWORLD_LAYOUT = layout("overworld");
    public static final ResourceKey<DimensionLayout> NETHER_LAYOUT = layout("the_nether");
    public static final ResourceKey<DimensionLayout> END_LAYOUT = layout("the_end");
    public static final ResourceKey<DimensionLayout> MIRROR_SEA_LAYOUT = layout("mirror_sea");
    public static final ResourceKey<DimensionLayout> POOLROOMS_LAYOUT = layout("poolrooms");
    public static final ResourceKey<DimensionLayout> BACKROOMS_LAYOUT = layout("backrooms");

    public static final ResourceKey<LevelStem> MIRROR_SEA_STEM = ResourceKey.create(Registries.LEVEL_STEM, Oneirgeo.id("mirror_sea"));
    public static final ResourceKey<LevelStem> POOLROOMS_STEM = ResourceKey.create(Registries.LEVEL_STEM, Oneirgeo.id("poolrooms"));
    public static final ResourceKey<LevelStem> BACKROOMS_STEM = ResourceKey.create(Registries.LEVEL_STEM, Oneirgeo.id("backrooms"));

    public static final ResourceKey<Level> MIRROR_SEA = ResourceKey.create(Registries.DIMENSION, Oneirgeo.id("mirror_sea"));
    public static final ResourceKey<Level> POOLROOMS = ResourceKey.create(Registries.DIMENSION, Oneirgeo.id("poolrooms"));
    public static final ResourceKey<Level> BACKROOMS = ResourceKey.create(Registries.DIMENSION, Oneirgeo.id("backrooms"));

    private OneirgeoDimensions() {
    }

    private static ResourceKey<DimensionType> type(String name) {
        return ResourceKey.create(Registries.DIMENSION_TYPE, Oneirgeo.id(name));
    }

    private static ResourceKey<DimensionLayout> layout(String name) {
        return ResourceKey.create(DimensionLayout.REGISTRY_KEY, Oneirgeo.id(name));
    }

    public static boolean isDream(ResourceKey<Level> dimension) {
        return dimension == MIRROR_SEA || dimension == POOLROOMS || dimension == BACKROOMS;
    }
}
