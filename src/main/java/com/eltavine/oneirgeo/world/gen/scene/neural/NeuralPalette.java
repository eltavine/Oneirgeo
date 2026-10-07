package com.eltavine.oneirgeo.world.gen.scene.neural;

import com.eltavine.oneirgeo.util.Hash;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Dark grey, brown, a little pale and a lot of dried blood; every block of tissue is one of them, picked at random. */
final class NeuralPalette {
    private static final BlockState[] BLOCKS = {
            Blocks.DEEPSLATE.defaultBlockState(),
            Blocks.DEEPSLATE.defaultBlockState(),
            Blocks.DEEPSLATE.defaultBlockState(),
            Blocks.COBBLED_DEEPSLATE.defaultBlockState(),
            Blocks.TUFF.defaultBlockState(),
            Blocks.TUFF.defaultBlockState(),
            Blocks.STONE.defaultBlockState(),
            Blocks.ANDESITE.defaultBlockState(),
            Blocks.SMOOTH_BASALT.defaultBlockState(),
            Blocks.SMOOTH_BASALT.defaultBlockState(),
            Blocks.BLACKSTONE.defaultBlockState(),
            Blocks.BONE_BLOCK.defaultBlockState(),
            Blocks.MUSHROOM_STEM.defaultBlockState(),
            Blocks.MUD.defaultBlockState(),
            Blocks.MUD.defaultBlockState(),
            Blocks.PACKED_MUD.defaultBlockState(),
            Blocks.MUD_BRICKS.defaultBlockState(),
            Blocks.DRIPSTONE_BLOCK.defaultBlockState(),
            Blocks.COARSE_DIRT.defaultBlockState(),
            Blocks.ROOTED_DIRT.defaultBlockState(),
            Blocks.TERRACOTTA.defaultBlockState(),
            Blocks.DYED_TERRACOTTA.pick(DyeColor.BROWN).defaultBlockState(),
            Blocks.DYED_TERRACOTTA.pick(DyeColor.BROWN).defaultBlockState(),
            Blocks.DYED_TERRACOTTA.pick(DyeColor.GRAY).defaultBlockState(),
            Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_GRAY).defaultBlockState(),
            Blocks.DYED_TERRACOTTA.pick(DyeColor.RED).defaultBlockState(),
            Blocks.DYED_TERRACOTTA.pick(DyeColor.RED).defaultBlockState(),
            Blocks.WOOL.pick(DyeColor.GRAY).defaultBlockState(),
            Blocks.WOOL.pick(DyeColor.BROWN).defaultBlockState(),
            Blocks.CONCRETE.pick(DyeColor.GRAY).defaultBlockState(),
            Blocks.DEAD_BRAIN_CORAL_BLOCK.defaultBlockState(),
            Blocks.DEAD_TUBE_CORAL_BLOCK.defaultBlockState(),
            Blocks.NETHER_WART_BLOCK.defaultBlockState(),
            Blocks.NETHER_WART_BLOCK.defaultBlockState(),
            Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState(),
            Blocks.CRIMSON_HYPHAE.defaultBlockState(),
    };
    /** The inner wall of a hollow soma. */
    private static final BlockState[] FLESH = {
            Blocks.NETHER_WART_BLOCK.defaultBlockState(),
            Blocks.NETHER_WART_BLOCK.defaultBlockState(),
            Blocks.CRIMSON_HYPHAE.defaultBlockState(),
            Blocks.DYED_TERRACOTTA.pick(DyeColor.RED).defaultBlockState(),
            Blocks.DYED_TERRACOTTA.pick(DyeColor.PINK).defaultBlockState(),
            Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState(),
            Blocks.MUD.defaultBlockState(),
            Blocks.PACKED_MUD.defaultBlockState(),
    };

    private NeuralPalette() {
    }

    static BlockState at(long seed, int x, int y, int z) {
        return BLOCKS[(int) Math.floorMod(Hash.of(seed, x, y, z), (long) BLOCKS.length)];
    }

    static BlockState flesh(long seed, int x, int y, int z) {
        return FLESH[(int) Math.floorMod(Hash.of(seed, x, y, z), (long) FLESH.length)];
    }
}
