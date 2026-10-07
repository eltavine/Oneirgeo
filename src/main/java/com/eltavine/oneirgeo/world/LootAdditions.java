package com.eltavine.oneirgeo.world;

import com.eltavine.oneirgeo.registry.OneirgeoItems;
import com.eltavine.oneirgeo.story.ChapterOfDimensionFunction;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

/** Vanilla chests that the dimensions reuse, with a few dream things slipped in. */
public final class LootAdditions {
    /** Chests the dimensions reuse, where a memory may have been left behind. */
    private static final java.util.Set<net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> FRAGMENTS = java.util.Set.of(
            BuiltInLootTables.VILLAGE_PLAINS_HOUSE, BuiltInLootTables.SIMPLE_DUNGEON, BuiltInLootTables.ABANDONED_MINESHAFT,
            BuiltInLootTables.STRONGHOLD_CORRIDOR, BuiltInLootTables.STRONGHOLD_LIBRARY, BuiltInLootTables.ANCIENT_CITY,
            BuiltInLootTables.SHIPWRECK_SUPPLY, BuiltInLootTables.NETHER_BRIDGE, BuiltInLootTables.DESERT_PYRAMID,
            BuiltInLootTables.END_CITY_TREASURE);

    private LootAdditions() {
    }

    public static void init() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }
            if (key.equals(BuiltInLootTables.END_CITY_TREASURE)) {
                table.withPool(LootPool.lootPool()
                        .when(LootItemRandomChanceCondition.randomChance(0.6F))
                        .add(LootItem.lootTableItem(OneirgeoItems.MIRROR_SHARD)));
            }
            if (FRAGMENTS.contains(key)) {
                table.withPool(LootPool.lootPool()
                        .when(LootItemRandomChanceCondition.randomChance(0.45F))
                        .add(LootItem.lootTableItem(OneirgeoItems.MEMORY_FRAGMENT).apply(ChapterOfDimensionFunction.chapterOfDimension())));
            }
        });
    }
}
