package com.eltavine.oneirgeo.story;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.registry.OneirgeoComponents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/** Loot function: the item belongs to the chapter of the dimension whose chest it was found in. */
public final class ChapterOfDimensionFunction extends LootItemConditionalFunction {
    public static final MapCodec<ChapterOfDimensionFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> commonFields(i).apply(i, ChapterOfDimensionFunction::new));

    private ChapterOfDimensionFunction(Optional<Holder<LootItemCondition>> condition) {
        super(condition);
    }

    public static void init() {
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Oneirgeo.id("chapter_of_dimension"), MAP_CODEC);
    }

    public static LootItemConditionalFunction.Builder<?> chapterOfDimension() {
        return simpleBuilder(ChapterOfDimensionFunction::new);
    }

    @Override
    public MapCodec<ChapterOfDimensionFunction> codec() {
        return MAP_CODEC;
    }

    @Override
    public ItemStack run(ItemStack stack, LootContext context) {
        stack.set(OneirgeoComponents.CHAPTER, Chapter.of(context.getLevel().dimension()));
        return stack;
    }
}
