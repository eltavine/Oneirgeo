package com.eltavine.oneirgeo.registry;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.item.DreamJournalItem;
import com.eltavine.oneirgeo.item.KeepsakeItem;
import com.eltavine.oneirgeo.item.LucidTeaItem;
import com.eltavine.oneirgeo.item.MemoryFragmentItem;
import com.eltavine.oneirgeo.item.MirrorShardItem;
import com.eltavine.oneirgeo.item.VhsTapeItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class OneirgeoItems {
    static final List<Item> BLOCK_ITEMS = new ArrayList<>();
    private static final List<Item> ITEMS = new ArrayList<>();

    public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Oneirgeo.id("oneirgeo"));

    public static final Item LUCID_TEA = register("lucid_tea", LucidTeaItem::new, new Item.Properties()
            .stacksTo(16)
            .component(DataComponents.CONSUMABLE, Consumables.defaultDrink().build())
            .usingConvertsTo(Items.GLASS_BOTTLE));
    public static final Item MIRROR_SHARD = register("mirror_shard", MirrorShardItem::new, new Item.Properties().stacksTo(16));
    public static final Item MEMORY_FRAGMENT = register("memory_fragment", MemoryFragmentItem::new, new Item.Properties().stacksTo(16));
    public static final Item DREAM_JOURNAL = register("dream_journal", DreamJournalItem::new, new Item.Properties().stacksTo(1));
    public static final Item VHS_TAPE = register("vhs_tape", VhsTapeItem::new, new Item.Properties().stacksTo(1));
    public static final Item GLASS_MARBLE = register("glass_marble", p -> new KeepsakeItem(p, "oneirgeo.marble.use"), new Item.Properties().stacksTo(1));
    public static final Item CHILD_DRAWING = register("child_drawing", p -> new KeepsakeItem(p, "oneirgeo.drawing.use"), new Item.Properties().stacksTo(1));

    private OneirgeoItems() {
    }

    static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Oneirgeo.id(name));
        Item item = Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
        ITEMS.add(item);
        return item;
    }

    /** Standalone items (not block items), in registration order. */
    public static List<Item> items() {
        return Collections.unmodifiableList(ITEMS);
    }

    public static List<Item> blockItems() {
        return Collections.unmodifiableList(BLOCK_ITEMS);
    }

    public static boolean isSpawnEgg(Item item) {
        return item instanceof net.minecraft.world.item.SpawnEggItem;
    }

    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.oneirgeo"))
                .icon(() -> new ItemStack(OneirgeoBlocks.MONOLITH_STONE))
                .displayItems((parameters, output) -> {
                    BLOCK_ITEMS.forEach(output::accept);
                    ITEMS.forEach(output::accept);
                })
                .build());
    }
}
