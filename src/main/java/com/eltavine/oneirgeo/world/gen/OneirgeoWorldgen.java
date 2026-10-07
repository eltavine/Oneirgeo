package com.eltavine.oneirgeo.world.gen;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.world.gen.scene.OneirgeoScenes;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class OneirgeoWorldgen {
    private OneirgeoWorldgen() {
    }

    public static void init() {
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, Oneirgeo.id("layered"), OneirgeoChunkGenerator.CODEC);
        Registry.register(BuiltInRegistries.BIOME_SOURCE, Oneirgeo.id("layered"), OneirgeoBiomeSource.CODEC);
        DynamicRegistries.register(DimensionLayout.REGISTRY_KEY, DimensionLayout.DIRECT_CODEC);
        OneirgeoScenes.init();
    }
}
