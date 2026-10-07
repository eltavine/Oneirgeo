package com.eltavine.oneirgeo.registry;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.block.ClockBlock;
import com.eltavine.oneirgeo.block.ElevatorBlock;
import com.eltavine.oneirgeo.block.FurnitureBlock;
import com.eltavine.oneirgeo.block.HeartMonitorBlock;
import com.eltavine.oneirgeo.block.MirrorPortalBlock;
import com.eltavine.oneirgeo.block.MirrorSurfaceBlock;
import com.eltavine.oneirgeo.block.PassageDoorBlock;
import com.eltavine.oneirgeo.block.TelephoneBlock;
import com.eltavine.oneirgeo.block.TelescopeBlock;
import com.eltavine.oneirgeo.block.TelevisionBlock;
import com.eltavine.oneirgeo.block.UpdraftBlock;
import com.eltavine.oneirgeo.block.UpdraftVentBlock;
import com.eltavine.oneirgeo.world.Passages;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class OneirgeoBlocks {
    private static final List<Block> ALL = new ArrayList<>();
    private static final List<Block> CUBES = new ArrayList<>();

    public static final Block MONOLITH_STONE = cube("monolith_stone", BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GRAY).strength(4.0F, 12.0F).sound(SoundType.POLISHED_DEEPSLATE).requiresCorrectToolForDrops());
    public static final Block CLOUD = cube("cloud", BlockBehaviour.Properties.of()
            .mapColor(MapColor.SNOW).strength(0.3F).sound(SoundType.WOOL).fallDistanceReduction(0.9F));
    public static final Block TEMPLE_MARBLE = cube("temple_marble", BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ).strength(2.0F, 6.0F).sound(SoundType.CALCITE).requiresCorrectToolForDrops());
    public static final Block FADED_PLASTER = cube("faded_plaster", BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_WHITE).strength(1.2F).sound(SoundType.CALCITE));
    public static final Block POOL_TILE = cube("pool_tile", BlockBehaviour.Properties.of()
            .mapColor(MapColor.SNOW).strength(1.5F, 6.0F).sound(SoundType.DEEPSLATE_TILES).requiresCorrectToolForDrops());
    public static final Block POOL_TILE_BLUE = cube("pool_tile_blue", BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_CYAN).strength(1.5F, 6.0F).sound(SoundType.DEEPSLATE_TILES).requiresCorrectToolForDrops());
    public static final Block POOL_LIGHT = cube("pool_light", BlockBehaviour.Properties.of()
            .mapColor(MapColor.SNOW).strength(0.6F).sound(SoundType.GLASS).lightLevel(s -> 15));
    public static final Block WALLPAPER = cube("wallpaper", BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_YELLOW).strength(1.0F).sound(SoundType.WOOL));
    public static final Block DAMP_CARPET = cube("damp_carpet", BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_YELLOW).strength(0.8F).sound(SoundType.MOSS_CARPET));
    public static final Block CEILING_TILE = cube("ceiling_tile", BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_WHITE).strength(0.6F).sound(SoundType.WOOL));
    public static final Block FLUORESCENT_LIGHT = register("fluorescent_light", com.eltavine.oneirgeo.block.FluorescentLightBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.SNOW).strength(0.4F).sound(SoundType.GLASS)
            .lightLevel(s -> s.getValue(com.eltavine.oneirgeo.block.FluorescentLightBlock.LIT) ? 14 : 0), true);
    public static final Block ASH = cube("ash", BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_GRAY).strength(0.5F).sound(SoundType.SAND));
    public static final Block COLD_LAVA = cube("cold_lava", BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_BLACK).strength(2.5F, 9.0F).sound(SoundType.BASALT).lightLevel(s -> 3).requiresCorrectToolForDrops());
    public static final Block BOILER_PLATE = cube("boiler_plate", BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_ORANGE).strength(3.0F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final Block EMBER = cube("ember", MagmaBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.FIRE).strength(1.0F).sound(SoundType.NETHERRACK).lightLevel(s -> 15).requiresCorrectToolForDrops());
    public static final Block STAR_STONE = cube("star_stone", BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_WHITE).strength(2.0F, 9.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());
    public static final Block GRAVE_STONE = cube("grave_stone", BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 9.0F).sound(SoundType.TUFF_BRICKS).requiresCorrectToolForDrops());
    public static final Block NIGHT_PLASTER = cube("night_plaster", BlockBehaviour.Properties.of()
            .mapColor(MapColor.SNOW).strength(1.2F).sound(SoundType.CALCITE));
    public static final Block SYNAPSE = cube("synapse", BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_PINK).strength(0.8F).sound(SoundType.FROGLIGHT).lightLevel(state -> 6));
    public static final Block TISSUE_EYE = cube("tissue_eye", BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_WHITE).strength(0.8F).sound(SoundType.FROGLIGHT).lightLevel(state -> 5));
    public static final Block MIRROR_SURFACE = register("mirror_surface", MirrorSurfaceBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.WATER).strength(0.4F).sound(SoundType.GLASS).noOcclusion().friction(0.98F)
            .isSuffocating((state, level, pos) -> false).pushReaction(PushReaction.IMMOVEABLE), true);
    public static final Block UPDRAFT = register("updraft", UpdraftBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.NONE).noCollision().noOcclusion().noLootTable().replaceable().pushReaction(PushReaction.POPPED)
            .isSuffocating((state, level, pos) -> false), false);
    public static final Block UPDRAFT_VENT = register("updraft_vent", UpdraftVentBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), true);
    public static final Block ELEVATOR = register("elevator", ElevatorBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL).strength(2.5F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops(), true);
    public static final Block STEAM_VENT = register("steam_vent", com.eltavine.oneirgeo.block.SteamVentBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GRAY).strength(3.0F, 8.0F).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops(), true);
    public static final Block MIRROR_FRAME = register("mirror_frame", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ).strength(2.0F, 6.0F).sound(SoundType.GLASS).requiresCorrectToolForDrops(), true);
    public static final Block MIRROR_PORTAL = register("mirror_portal", MirrorPortalBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.SNOW).noCollision().strength(-1.0F, 3600000.0F).sound(SoundType.GLASS).lightLevel(state -> 11)
            .noLootTable().noOcclusion().pushReaction(PushReaction.IMMOVEABLE), false);

    private static final List<Block> FURNITURE = new ArrayList<>();
    public static final Block TELEVISION = furniture("television", TelevisionBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GRAY).strength(1.5F).sound(SoundType.STONE)
            .lightLevel(state -> state.getValue(TelevisionBlock.PLAYING) ? 9 : 1));
    public static final Block TELEPHONE = furniture("telephone", TelephoneBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_WHITE).strength(0.6F).sound(SoundType.STONE));
    public static final Block STOPPED_CLOCK = furniture("stopped_clock", ClockBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD).strength(0.6F).sound(SoundType.WOOD).noCollision());
    public static final Block HOSPITAL_BED = furniture("hospital_bed", p -> new FurnitureBlock(p, FurnitureBlock.box(0, 0, 0, 16, 9, 16), "oneirgeo.bed.hospital"),
            BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(1.0F).sound(SoundType.WOOL));
    public static final Block IV_STAND = furniture("iv_stand", p -> new FurnitureBlock(p, FurnitureBlock.box(6, 0, 6, 10, 16, 10), "oneirgeo.iv.use"),
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.8F).sound(SoundType.CHAIN));
    public static final Block HEART_MONITOR = furniture("heart_monitor", HeartMonitorBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_GRAY).strength(1.0F).sound(SoundType.STONE).lightLevel(state -> 4));
    public static final Block WAITING_CHAIR = furniture("waiting_chair", p -> new FurnitureBlock(p, FurnitureBlock.box(1, 0, 1, 15, 15, 15), "oneirgeo.chair.waiting"),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(1.0F).sound(SoundType.STONE));
    public static final Block TELESCOPE = furniture("telescope", TelescopeBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_ORANGE).strength(1.5F).sound(SoundType.COPPER));

    private static final List<Block> DOORS = new ArrayList<>();
    public static final Block BACKROOMS_DOOR = door("backrooms_door", Passages.Kind.BACKROOMS, MapColor.TERRACOTTA_YELLOW);
    public static final Block EXIT_DOOR = door("exit_door", Passages.Kind.EXIT, MapColor.SNOW);
    public static final Block WAKE_DOOR = door("wake_door", Passages.Kind.WAKE, MapColor.COLOR_BLUE);
    public static final Block CLOSED_DOOR = door("closed_door", Passages.Kind.CLOSED, MapColor.COLOR_BLACK);

    private OneirgeoBlocks() {
    }

    private static Block cube(String name, BlockBehaviour.Properties properties) {
        return cube(name, Block::new, properties);
    }

    private static Block cube(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        Block block = register(name, factory, properties, true);
        CUBES.add(block);
        return block;
    }

    private static Block furniture(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        Block block = register(name, factory, properties.noOcclusion().pushReaction(PushReaction.POPPED), true);
        FURNITURE.add(block);
        return block;
    }

    /** Things left in rooms; each has a hand-made model facing north, rotated by its facing. */
    public static List<Block> furniture() {
        return Collections.unmodifiableList(FURNITURE);
    }

    private static Block door(String name, Passages.Kind kind, MapColor color) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Oneirgeo.id(name));
        Block block = Registry.register(BuiltInRegistries.BLOCK, key, new PassageDoorBlock(kind, BlockBehaviour.Properties.of()
                .mapColor(color).strength(-1.0F, 3600000.0F).sound(SoundType.WOOD).noOcclusion().noLootTable().pushReaction(PushReaction.IMMOVEABLE)
                .setId(key)));
        ALL.add(block);
        DOORS.add(block);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Oneirgeo.id(name));
        Item item = Registry.register(BuiltInRegistries.ITEM, itemKey, new DoubleHighBlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
        OneirgeoItems.BLOCK_ITEMS.add(item);
        return block;
    }

    public static List<Block> doors() {
        return Collections.unmodifiableList(DOORS);
    }

    static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties, boolean withItem) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Oneirgeo.id(name));
        Block block = Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
        ALL.add(block);
        if (withItem) {
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Oneirgeo.id(name));
            Item item = Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
            OneirgeoItems.BLOCK_ITEMS.add(item);
        }
        return block;
    }

    /** Every block of the mod, in registration order. */
    public static List<Block> all() {
        return Collections.unmodifiableList(ALL);
    }

    /** Plain full cubes that use a single texture on every face. */
    public static List<Block> cubes() {
        return Collections.unmodifiableList(CUBES);
    }

    public static void init() {
    }
}
