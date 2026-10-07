package com.eltavine.oneirgeo.client.datagen;

import com.eltavine.oneirgeo.world.gen.DimensionLayout;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public final class OneirgeoDatagen {
    private OneirgeoDatagen() {
    }

    public static void buildRegistry(RegistrySetBuilder builder) {
        builder.add(Registries.TIMELINE, DimensionTypeData::timelines);
        builder.add(Registries.DIMENSION_TYPE, DimensionTypeData::bootstrap);
        builder.add(Registries.BIOME, BiomeData::bootstrap);
        builder.add(DimensionLayout.REGISTRY_KEY, LayoutData::bootstrap);
        builder.add(Registries.LEVEL_STEM, LayoutData::stems);
    }

    public static void addProviders(FabricDataGenerator.Pack pack) {
        pack.addProvider(WorldDataProvider::new);
        pack.addProvider(TextureProvider::new);
        pack.addProvider(ModelData::new);
        pack.addProvider(LangData::english);
        pack.addProvider(LangData::chinese);
        pack.addProvider(BlockLootData::new);
        pack.addProvider(BlockTagData::new);
        pack.addProvider(SoundData::new);
    }
}
