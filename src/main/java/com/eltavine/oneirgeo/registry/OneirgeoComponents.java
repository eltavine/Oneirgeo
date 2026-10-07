package com.eltavine.oneirgeo.registry;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.story.Chapter;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class OneirgeoComponents {
    /** Which chapter a memory fragment belongs to: the dimension it was found in. */
    public static final DataComponentType<Chapter> CHAPTER = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Oneirgeo.id("chapter"),
            DataComponentType.<Chapter>builder().persistent(Chapter.CODEC).networkSynchronized(Chapter.STREAM_CODEC).build());
    /** Which recording a tape holds: a chapter id, or {@code capsule}. */
    public static final DataComponentType<String> TAPE = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Oneirgeo.id("tape"),
            DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());

    private OneirgeoComponents() {
    }

    public static void init() {
    }
}
