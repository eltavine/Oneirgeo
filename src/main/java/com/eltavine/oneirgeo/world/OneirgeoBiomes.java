package com.eltavine.oneirgeo.world;

import com.eltavine.oneirgeo.Oneirgeo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public final class OneirgeoBiomes {
    private static final List<ResourceKey<Biome>> ALL = new ArrayList<>();

    public static final ResourceKey<Biome> NEURAL_FOG = key("neural_fog");

    public static final ResourceKey<Biome> HANGING_CITY = key("hanging_city");
    public static final ResourceKey<Biome> GREAT_HEARTH = key("great_hearth");
    public static final ResourceKey<Biome> ASH_PLAINS = key("ash_plains");
    public static final ResourceKey<Biome> LAVA_SEA = key("lava_sea");
    public static final ResourceKey<Biome> BOILER_CORRIDORS = key("boiler_corridors");

    public static final ResourceKey<Biome> VOID_GEOMETRY = key("void_geometry");
    public static final ResourceKey<Biome> STAR_CEMETERY = key("star_cemetery");
    public static final ResourceKey<Biome> NIGHT_SEA = key("night_sea");

    public static final ResourceKey<Biome> MIRROR_SEA = key("mirror_sea");
    public static final ResourceKey<Biome> POOLROOMS = key("poolrooms");
    public static final ResourceKey<Biome> BACKROOMS = key("backrooms");

    /** Variant used inside closed loops (traps) of any dimension. */
    public static final ResourceKey<Biome> CLOSED_ROOM = key("closed_room");

    private OneirgeoBiomes() {
    }

    private static ResourceKey<Biome> key(String name) {
        ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, Oneirgeo.id(name));
        ALL.add(key);
        return key;
    }

    public static List<ResourceKey<Biome>> all() {
        return Collections.unmodifiableList(ALL);
    }
}
