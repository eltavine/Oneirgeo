package com.eltavine.oneirgeo.client;

import com.eltavine.oneirgeo.client.datagen.OneirgeoDatagen;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;

public class OneirgeoDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        OneirgeoDatagen.addProviders(fabricDataGenerator.createPack());
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        OneirgeoDatagen.buildRegistry(registryBuilder);
    }
}
