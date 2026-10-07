package com.eltavine.oneirgeo.client.datagen;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;

final class BlockTagData extends FabricTagsProvider.BlockTagsProvider {
    BlockTagData(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    private static ResourceKey<Block> key(Block block) {
        return BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow();
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        for (Block block : List.of(OneirgeoBlocks.MONOLITH_STONE, OneirgeoBlocks.TEMPLE_MARBLE, OneirgeoBlocks.FADED_PLASTER,
                OneirgeoBlocks.POOL_TILE, OneirgeoBlocks.POOL_TILE_BLUE, OneirgeoBlocks.COLD_LAVA, OneirgeoBlocks.BOILER_PLATE,
                OneirgeoBlocks.EMBER, OneirgeoBlocks.STAR_STONE, OneirgeoBlocks.GRAVE_STONE, OneirgeoBlocks.NIGHT_PLASTER,
                OneirgeoBlocks.UPDRAFT_VENT, OneirgeoBlocks.MIRROR_SURFACE, OneirgeoBlocks.POOL_LIGHT, OneirgeoBlocks.FLUORESCENT_LIGHT,
                OneirgeoBlocks.MIRROR_FRAME)) {
            this.builder(BlockTags.MINEABLE_WITH_PICKAXE).add(key(block));
        }
        this.builder(BlockTags.MINEABLE_WITH_SHOVEL).add(key(OneirgeoBlocks.ASH)).add(key(OneirgeoBlocks.CLOUD));
        this.builder(BlockTags.MINEABLE_WITH_AXE).add(key(OneirgeoBlocks.WALLPAPER)).add(key(OneirgeoBlocks.DAMP_CARPET)).add(key(OneirgeoBlocks.CEILING_TILE));
        // Heightmaps (respawning, mob spawns, rain) only see blocks in this tag since 26.x.
        for (Block block : OneirgeoBlocks.all()) {
            if (block != OneirgeoBlocks.UPDRAFT && block != OneirgeoBlocks.MIRROR_PORTAL) {
                this.builder(BlockTags.BLOCKS_MOTION_NO_LEAVES).add(key(block));
            }
        }
        this.builder(BlockTags.INFINIBURN_OVERWORLD).add(key(OneirgeoBlocks.EMBER));
        this.builder(BlockTags.INFINIBURN_NETHER).add(key(OneirgeoBlocks.EMBER));
        this.builder(BlockTags.INFINIBURN_END).add(key(OneirgeoBlocks.EMBER));
    }
}
