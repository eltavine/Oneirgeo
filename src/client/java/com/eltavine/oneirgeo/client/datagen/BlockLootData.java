package com.eltavine.oneirgeo.client.datagen;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;

final class BlockLootData extends FabricBlockLootSubProvider {
    BlockLootData(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void generate() {
        for (Block block : OneirgeoBlocks.all()) {
            if (block.getLootTable().isPresent() && block.asItem() != net.minecraft.world.item.Items.AIR) {
                this.dropSelf(block);
            }
        }
    }
}
