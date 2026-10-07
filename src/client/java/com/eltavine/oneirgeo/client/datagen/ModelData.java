package com.eltavine.oneirgeo.client.datagen;

import com.eltavine.oneirgeo.block.TelevisionBlock;
import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.registry.OneirgeoItems;
import java.util.Set;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

final class ModelData extends FabricModelProvider {
    ModelData(FabricPackOutput output) {
        super(output);
    }

    /** Blocks with hand-picked models; everything else in {@link OneirgeoBlocks#cubes()} is a plain cube. */
    static Set<Block> special() {
        return Set.of(OneirgeoBlocks.MIRROR_SURFACE, OneirgeoBlocks.UPDRAFT, OneirgeoBlocks.MIRROR_PORTAL, OneirgeoBlocks.FLUORESCENT_LIGHT);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generators) {
        for (Block block : OneirgeoBlocks.cubes()) {
            generators.createTrivialCube(block);
        }
        generators.createTrivialBlock(OneirgeoBlocks.MIRROR_SURFACE, TexturedModel.CARPET);
        generators.createParticleOnlyBlock(OneirgeoBlocks.UPDRAFT, OneirgeoBlocks.CLOUD);
        generators.blockStateOutput.accept(MultiVariantGenerator.dispatch(OneirgeoBlocks.MIRROR_PORTAL)
                .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_AXIS)
                        .select(Direction.Axis.X, BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(OneirgeoBlocks.MIRROR_PORTAL, "_ns")))
                        .select(Direction.Axis.Z, BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(OneirgeoBlocks.MIRROR_PORTAL, "_ew")))));
        Identifier lit = TexturedModel.CUBE.create(OneirgeoBlocks.FLUORESCENT_LIGHT, generators.modelOutput);
        Identifier dark = ModelTemplates.CUBE_ALL.createWithSuffix(OneirgeoBlocks.FLUORESCENT_LIGHT, "_off",
                TextureMapping.cube(TextureMapping.getBlockTexture(OneirgeoBlocks.FLUORESCENT_LIGHT, "_off")), generators.modelOutput);
        generators.blockStateOutput.accept(MultiVariantGenerator.dispatch(OneirgeoBlocks.FLUORESCENT_LIGHT)
                .with(PropertyDispatch.initial(com.eltavine.oneirgeo.block.FluorescentLightBlock.LIT)
                        .select(true, BlockModelGenerators.plainVariant(lit))
                        .select(false, BlockModelGenerators.plainVariant(dark))));
        for (Block door : OneirgeoBlocks.doors()) {
            generators.createDoor(door);
        }
        for (Block block : OneirgeoBlocks.furniture()) {
            Identifier model = ModelLocationUtils.getModelLocation(block);
            if (block == OneirgeoBlocks.TELEVISION) {
                generators.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(TelevisionBlock.PLAYING)
                                .select(false, BlockModelGenerators.plainVariant(model))
                                .select(true, BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(block, "_on"))))
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
            } else {
                generators.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model))
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
            }
            generators.registerSimpleItemModel(block, model);
        }
        for (Block block : OneirgeoBlocks.all()) {
            if (!OneirgeoBlocks.cubes().contains(block) && !special().contains(block) && !OneirgeoBlocks.doors().contains(block)
                    && !OneirgeoBlocks.furniture().contains(block)) {
                generators.createTrivialCube(block);
            }
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerators generators) {
        for (Item item : OneirgeoItems.items()) {
            if (!OneirgeoItems.isSpawnEgg(item)) {
                generators.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
            }
        }
    }
}
