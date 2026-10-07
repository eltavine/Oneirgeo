package com.eltavine.oneirgeo.client.datagen;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import com.eltavine.oneirgeo.world.OneirgeoDimensions;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.AmbientMoodSettings;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.timeline.Timeline;
import java.util.List;

/** Dimension types: every dimension spans Y -2032..2031; time is frozen into a single moment or drifts. */
final class DimensionTypeData {
    static final ResourceKey<Timeline> END_DRIFT = ResourceKey.create(Registries.TIMELINE, Oneirgeo.id("end_drift"));

    static final BedRule DREAM_BED = new BedRule(BedRule.Rule.ALWAYS, BedRule.Rule.ALWAYS, false, false, Optional.empty());
    static final BedRule NO_BED = new BedRule(BedRule.Rule.NEVER, BedRule.Rule.NEVER, false, false,
            Optional.of(Component.translatable("oneirgeo.bed.no_sleep")));

    private DimensionTypeData() {
    }

    static Music music(net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound, int min, int max) {
        return new Music(sound, min, max, false);
    }

    static void timelines(BootstrapContext<Timeline> context) {
        HolderGetter<WorldClock> clocks = context.lookup(Registries.WORLD_CLOCK);
        context.register(END_DRIFT, Timeline.builder(clocks.getOrThrow(WorldClocks.THE_END))
                .setPeriodTicks(24000)
                .addTrack(EnvironmentAttributes.STAR_ANGLE, track -> track.addKeyframe(0, 360.0F).addKeyframe(0, 0.0F))
                .addTrack(EnvironmentAttributes.MOON_ANGLE, track -> track.addKeyframe(0, 380.0F).addKeyframe(0, 20.0F))
                .build());
    }

    static void bootstrap(BootstrapContext<DimensionType> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        HolderGetter<Timeline> timelines = context.lookup(Registries.TIMELINE);
        HolderGetter<WorldClock> clocks = context.lookup(Registries.WORLD_CLOCK);
        int minY = OneirgeoDimensions.MIN_Y;
        int height = OneirgeoDimensions.HEIGHT;

        EnvironmentAttributeMap overworld = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.SUN_ANGLE, 180.0F)
                .set(EnvironmentAttributes.MOON_ANGLE, 180.0F)
                .set(EnvironmentAttributes.STAR_BRIGHTNESS, 0.0F)
                .set(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x242120))
                .set(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(0x2B2826))
                .set(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, ARGB.vector4fFromARGB32(0x00000000))
                .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.vector4fFromARGB32(0x00000000))
                .set(EnvironmentAttributes.SKY_LIGHT_COLOR, ARGB.vector3fFromRGB24(0xA8988E))
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.4F)
                .set(EnvironmentAttributes.SKY_LIGHT_LEVEL, 5.0F)
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ARGB.vector3fFromRGB24(0x0A0A09))
                .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(Optional.of(music(OneirgeoSounds.MUSIC_DEEP, 9000, 22000)), Optional.empty(), Optional.empty()))
                .set(EnvironmentAttributes.BED_RULE, DREAM_BED)
                .set(EnvironmentAttributes.STRAW_BED_RULE, DREAM_BED)
                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, false)
                .set(EnvironmentAttributes.CAN_START_RAID, false)
                .set(EnvironmentAttributes.CAN_PILLAGER_PATROL_SPAWN, false)
                .set(EnvironmentAttributes.MONSTERS_BURN, false)
                .build();
        context.register(OneirgeoDimensions.OVERWORLD_TYPE, new DimensionType(
                true, true, false, false, 1.0, minY, height, height,
                blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD), 0.04F,
                new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
                DimensionType.Skybox.OVERWORLD, CardinalLighting.Type.DEFAULT, overworld,
                HolderSet.empty(), Optional.of(clocks.getOrThrow(WorldClocks.OVERWORLD))));

        EnvironmentAttributeMap nether = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.FOG_START_DISTANCE, 12.0F)
                .set(EnvironmentAttributes.FOG_END_DISTANCE, 160.0F)
                .set(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(0x2E1C16))
                .set(EnvironmentAttributes.SKY_LIGHT_COLOR, ARGB.vector3fFromRGB24(0x7A7AFF))
                .set(EnvironmentAttributes.SKY_LIGHT_LEVEL, 3.0F)
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.0F)
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ARGB.vector3fFromRGB24(0x231D19))
                .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(Optional.of(music(OneirgeoSounds.MUSIC_FURNACE, 9000, 20000)), Optional.empty(), Optional.empty()))
                .set(EnvironmentAttributes.BED_RULE, BedRule.DESTROY_ON_USE)
                .set(EnvironmentAttributes.STRAW_BED_RULE, BedRule.DESTROY_ON_USE)
                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, true)
                .set(EnvironmentAttributes.WATER_EVAPORATES, true)
                .set(EnvironmentAttributes.FAST_LAVA, true)
                .set(EnvironmentAttributes.PIGLINS_ZOMBIFY, false)
                .set(EnvironmentAttributes.CAN_START_RAID, false)
                .set(EnvironmentAttributes.SNOW_GOLEM_MELTS, true)
                .build();
        context.register(OneirgeoDimensions.NETHER_TYPE, new DimensionType(
                true, false, true, false, 8.0, minY, height, height,
                blocks.getOrThrow(BlockTags.INFINIBURN_NETHER), 0.07F,
                new DimensionType.MonsterSettings(ConstantInt.of(7), 15),
                DimensionType.Skybox.NONE, CardinalLighting.Type.NETHER, nether,
                HolderSet.empty(), Optional.empty()));

        EnvironmentAttributeMap end = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.SUN_ANGLE, 180.0F)
                .set(EnvironmentAttributes.MOON_ANGLE, 20.0F)
                .set(EnvironmentAttributes.MOON_PHASE, MoonPhase.FULL_MOON)
                .set(EnvironmentAttributes.STAR_BRIGHTNESS, 0.6F)
                .set(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x050407))
                .set(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(0x0D0B10))
                .set(EnvironmentAttributes.SKY_LIGHT_COLOR, ARGB.vector3fFromRGB24(0x9A8EAE))
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.2F)
                .set(EnvironmentAttributes.SKY_LIGHT_LEVEL, 4.0F)
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ARGB.vector3fFromRGB24(0x3F473F))
                .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.vector4fFromARGB32(0x00000000))
                .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(Optional.of(music(OneirgeoSounds.MUSIC_VOID, 6000, 20000)), Optional.empty(), Optional.empty()))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                .set(EnvironmentAttributes.BED_RULE, NO_BED)
                .set(EnvironmentAttributes.STRAW_BED_RULE, NO_BED)
                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                .build();
        context.register(OneirgeoDimensions.END_TYPE, new DimensionType(
                true, true, false, false, 1.0, minY, height, height,
                blocks.getOrThrow(BlockTags.INFINIBURN_END), 0.25F,
                new DimensionType.MonsterSettings(ConstantInt.of(15), 0),
                DimensionType.Skybox.OVERWORLD, CardinalLighting.Type.DEFAULT, end,
                HolderSet.direct(timelines.getOrThrow(END_DRIFT)), Optional.of(clocks.getOrThrow(WorldClocks.THE_END))));

        EnvironmentAttributeMap mirror = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.SUN_ANGLE, 180.0F)
                .set(EnvironmentAttributes.MOON_ANGLE, 180.0F)
                .set(EnvironmentAttributes.STAR_BRIGHTNESS, 0.0F)
                .set(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x8C9196))
                .set(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(0x9A9DA0))
                .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.vector4fFromARGB32(0xE8A2A6AA))
                .set(EnvironmentAttributes.CLOUD_HEIGHT, 160.0F)
                .set(EnvironmentAttributes.SKY_LIGHT_COLOR, ARGB.vector3fFromRGB24(0xC8CCD0))
                .set(EnvironmentAttributes.SKY_LIGHT_LEVEL, 11.0F)
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.75F)
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ARGB.vector3fFromRGB24(0x202024))
                .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(Optional.of(music(OneirgeoSounds.MUSIC_DREAM, 4000, 12000)), Optional.empty(), Optional.empty()))
                .set(EnvironmentAttributes.BED_RULE, NO_BED)
                .set(EnvironmentAttributes.STRAW_BED_RULE, NO_BED)
                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                .set(EnvironmentAttributes.MONSTERS_BURN, false)
                .build();
        context.register(OneirgeoDimensions.MIRROR_SEA_TYPE, new DimensionType(
                true, true, false, false, 1.0, minY, height, height,
                blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD), 0.3F,
                new DimensionType.MonsterSettings(ConstantInt.of(0), 0),
                DimensionType.Skybox.OVERWORLD, CardinalLighting.Type.DEFAULT, mirror,
                HolderSet.empty(), Optional.empty()));

        context.register(OneirgeoDimensions.POOLROOMS_TYPE, new DimensionType(
                true, false, true, false, 1.0, minY, height, height,
                blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD), 0.42F,
                new DimensionType.MonsterSettings(ConstantInt.of(0), 0),
                DimensionType.Skybox.NONE, CardinalLighting.Type.DEFAULT, indoor(0xB5C2C0, 0x101418, OneirgeoSounds.MUSIC_DREAM),
                HolderSet.empty(), Optional.empty()));

        context.register(OneirgeoDimensions.BACKROOMS_TYPE, new DimensionType(
                true, false, true, false, 1.0, minY, height, height,
                blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD), 0.32F,
                new DimensionType.MonsterSettings(ConstantInt.of(0), 0),
                DimensionType.Skybox.NONE, CardinalLighting.Type.DEFAULT, indoor(0x6E6233, 0x14110A, OneirgeoSounds.MUSIC_DEEP),
                HolderSet.empty(), Optional.empty()));
    }

    private static EnvironmentAttributeMap indoor(int fog, int ambient, net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> music) {
        return EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(fog))
                .set(EnvironmentAttributes.SKY_LIGHT_LEVEL, 0.0F)
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.0F)
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ARGB.vector3fFromRGB24(ambient))
                .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(Optional.of(music(music, 8000, 20000)), Optional.empty(), Optional.empty()))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.empty(),
                        Optional.of(new AmbientMoodSettings(OneirgeoSounds.AMBIENT_ROOMS_MOOD, 4000, 8, 2.0)), List.of()))
                .set(EnvironmentAttributes.BED_RULE, NO_BED)
                .set(EnvironmentAttributes.STRAW_BED_RULE, NO_BED)
                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                .set(EnvironmentAttributes.CAN_START_RAID, false)
                .build();
    }
}
