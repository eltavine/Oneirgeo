package com.eltavine.oneirgeo.client.datagen;

import com.eltavine.oneirgeo.world.gen.DimensionLayout;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Writes every dynamic registry entry of the mod and replaces {@code minecraft:normal}. */
final class WorldDataProvider extends FabricDynamicRegistryProvider {
    WorldDataProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        entries.addAll(registries.lookupOrThrow(Registries.TIMELINE));
        entries.addAll(registries.lookupOrThrow(Registries.DIMENSION_TYPE));
        entries.addAll(registries.lookupOrThrow(Registries.BIOME));
        entries.addAll(registries.lookupOrThrow(DimensionLayout.REGISTRY_KEY));
        entries.addAll(registries.lookupOrThrow(Registries.LEVEL_STEM));
        entries.add(WorldPresets.NORMAL, LayoutData.normalPreset(
                registries.lookupOrThrow(Registries.DIMENSION_TYPE), registries.lookupOrThrow(DimensionLayout.REGISTRY_KEY)));
    }

    @Override
    public String getName() {
        return "Oneirgeo world data";
    }
}
