package com.eltavine.oneirgeo.client.datagen;

import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import com.eltavine.oneirgeo.world.OneirgeoBiomes;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.AmbientAdditionsSettings;
import net.minecraft.world.attribute.AmbientMoodSettings;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;

/**
 * One biome per scene. Biomes carry no features; they exist for fog, sky, light, sound and the
 * deliberately thin spawn lists.
 */
final class BiomeData {
    private BiomeData() {
    }

    private record Look(int fog, float fogStart, float fogEnd, int water, int waterFog, float waterFogEnd) {
    }

    static void bootstrap(BootstrapContext<Biome> context) {
        BiomeGenerationSettings empty = BiomeGenerationSettings.EMPTY;

        register(context, OneirgeoBiomes.NEURAL_FOG, empty, new Look(0x2B2826, 4.0F, 74.0F, 0x2C2222, 0x0C0808, 20.0F), a -> a
                .set(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x242120))
                .set(EnvironmentAttributes.AMBIENT_PARTICLES, particles(ParticleTypes.WHITE_ASH, 0.0016F))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.of(OneirgeoSounds.AMBIENT_FOG), Optional.empty(), List.of(
                        new AmbientAdditionsSettings(OneirgeoSounds.AMBIENT_PULSE, 0.0016),
                        new AmbientAdditionsSettings(OneirgeoSounds.AMBIENT_CURRENT, 0.0007),
                        new AmbientAdditionsSettings(OneirgeoSounds.WHISPER, 0.0005)))), mobs -> mobs
                .addSpawn(EntityTypes.ZOMBIE, 3, 1, 1)
                .addSpawn(EntityTypes.SPIDER, 2, 1, 1)
                .addMobSpawnCost(EntityTypes.ZOMBIE, 0.8, 0.1)
                .addMobSpawnCost(EntityTypes.SPIDER, 0.8, 0.1), 0x5E5A50, 0x55514A);

        register(context, OneirgeoBiomes.HANGING_CITY, empty, new Look(0x2A1714, 10.0F, 170.0F, 0x905957, 0x050533, 32.0F), a -> a
                .set(EnvironmentAttributes.AMBIENT_PARTICLES, particles(ParticleTypes.ASH, 0.008F))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, loop(OneirgeoSounds.AMBIENT_FURNACE, null)), mobs -> {
                }, 0x8A6040, 0x7A5030);
        register(context, OneirgeoBiomes.GREAT_HEARTH, empty, new Look(0x3A2018, 16.0F, 340.0F, 0x905957, 0x050533, 32.0F), a -> a
                .set(EnvironmentAttributes.AMBIENT_PARTICLES, particles(ParticleTypes.ASH, 0.004F))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, loop(OneirgeoSounds.AMBIENT_FURNACE, null)), mobs -> mobs
                .addSpawn(EntityTypes.GHAST, 1, 1, 1)
                .addMobSpawnCost(EntityTypes.GHAST, 0.9, 0.08), 0x8A6040, 0x7A5030);
        register(context, OneirgeoBiomes.ASH_PLAINS, empty, new Look(0x45423F, 4.0F, 120.0F, 0x905957, 0x050533, 32.0F), a -> a
                .set(EnvironmentAttributes.AMBIENT_PARTICLES, particles(ParticleTypes.WHITE_ASH, 0.03F))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.of(OneirgeoSounds.AMBIENT_FURNACE),
                        Optional.of(new AmbientMoodSettings(OneirgeoSounds.AMBIENT_FURNACE_MOOD, 6000, 8, 2.0)), List.of())), mobs -> mobs
                .addSpawn(EntityTypes.WITHER_SKELETON, 1, 1, 1)
                .addSpawn(EntityTypes.MAGMA_CUBE, 1, 1, 1)
                .addMobSpawnCost(EntityTypes.WITHER_SKELETON, 0.8, 0.1)
                .addMobSpawnCost(EntityTypes.MAGMA_CUBE, 0.8, 0.1), 0x8A8580, 0x7A7570);
        register(context, OneirgeoBiomes.LAVA_SEA, empty, new Look(0x4A2A1C, 6.0F, 140.0F, 0x905957, 0x050533, 32.0F), a -> a
                .set(EnvironmentAttributes.AMBIENT_PARTICLES, particles(ParticleTypes.ASH, 0.012F))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, loop(OneirgeoSounds.AMBIENT_FURNACE, null)), mobs -> mobs
                .addSpawn(EntityTypes.MAGMA_CUBE, 2, 1, 1)
                .addMobSpawnCost(EntityTypes.MAGMA_CUBE, 0.8, 0.1), 0x8A6040, 0x7A5030);
        register(context, OneirgeoBiomes.BOILER_CORRIDORS, empty, new Look(0x1E0C0A, 1.0F, 40.0F, 0x905957, 0x050533, 32.0F), a -> a
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.of(OneirgeoSounds.AMBIENT_HUM),
                        Optional.of(new AmbientMoodSettings(OneirgeoSounds.AMBIENT_FURNACE_MOOD, 3000, 8, 2.0)), List.of())), mobs -> mobs
                .addSpawn(EntityTypes.BLAZE, 1, 1, 1)
                .addMobSpawnCost(EntityTypes.BLAZE, 0.9, 0.08), 0x8A6040, 0x7A5030);

        register(context, OneirgeoBiomes.VOID_GEOMETRY, empty, new Look(0x0A090D, 40.0F, 520.0F, 0x3F3F7E, 0x050510, 64.0F), a -> a
                .set(EnvironmentAttributes.AMBIENT_PARTICLES, particles(ParticleTypes.END_ROD, 0.00025F))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, loop(OneirgeoSounds.AMBIENT_VOID, null)), mobs -> mobs
                .addSpawn(EntityTypes.ENDERMAN, 2, 1, 1), 0x8E8EA0, 0x7E7E90);
        register(context, OneirgeoBiomes.STAR_CEMETERY, empty, new Look(0x121014, 24.0F, 230.0F, 0x3F3F7E, 0x050510, 64.0F), a -> a
                .set(EnvironmentAttributes.AMBIENT_PARTICLES, particles(ParticleTypes.REVERSE_PORTAL, 0.0018F))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, loop(OneirgeoSounds.AMBIENT_VOID, new AmbientAdditionsSettings(OneirgeoSounds.WHISPER, 0.0006))), mobs -> mobs
                .addSpawn(EntityTypes.ENDERMAN, 10, 1, 2), 0x8E8EA0, 0x7E7E90);
        register(context, OneirgeoBiomes.NIGHT_SEA, empty, new Look(0x040405, 20.0F, 320.0F, 0x0B0B12, 0x020204, 18.0F), a -> a
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, loop(OneirgeoSounds.AMBIENT_SEA, null)), mobs -> mobs
                .addSpawn(EntityTypes.GLOW_SQUID, 1, 1, 1), 0x8E8EA0, 0x7E7E90);

        register(context, OneirgeoBiomes.MIRROR_SEA, empty, new Look(0x9A9DA0, 40.0F, 420.0F, 0x7A8A92, 0x8A969C, 96.0F), a -> a
                .set(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x8C9196))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, loop(OneirgeoSounds.AMBIENT_SEA, null)), mobs -> {
        }, 0xB8E0A0, 0xA8D090);
        register(context, OneirgeoBiomes.POOLROOMS, empty, new Look(0xB9C6C4, 6.0F, 96.0F, 0x5EE0FF, 0xA6F0FF, 64.0F), a -> a
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.of(OneirgeoSounds.AMBIENT_POOL), Optional.empty(),
                        List.of(new AmbientAdditionsSettings(OneirgeoSounds.AMBIENT_POOL_DRIP, 0.004)))), mobs -> {
        }, 0xA0D0C0, 0x90C0B0);
        register(context, OneirgeoBiomes.BACKROOMS, empty, new Look(0x6E6233, 2.0F, 64.0F, 0x3F76E4, 0x050533, 32.0F), a -> a
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.of(OneirgeoSounds.AMBIENT_HUM),
                        Optional.of(new AmbientMoodSettings(OneirgeoSounds.AMBIENT_ROOMS_MOOD, 3600, 8, 2.0)), List.of())), mobs -> {
        }, 0xA09A60, 0x908A50);
        register(context, OneirgeoBiomes.CLOSED_ROOM, empty, new Look(0x000000, 0.0F, 22.0F, 0x101010, 0x000000, 8.0F), a -> a
                .set(EnvironmentAttributes.BED_RULE, DimensionTypeData.NO_BED)
                .set(EnvironmentAttributes.STRAW_BED_RULE, DimensionTypeData.NO_BED)
                .set(EnvironmentAttributes.MUSIC_VOLUME, 0.0F)
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, loop(OneirgeoSounds.AMBIENT_CLOSED, null)), mobs -> {
        }, 0x707070, 0x606060);
    }

    private static void register(BootstrapContext<Biome> context, ResourceKey<Biome> key, BiomeGenerationSettings generation, Look look,
                                 Consumer<EnvironmentAttributeMap.Builder> attributes, Consumer<MobSpawnSettings.Builder> spawns,
                                 int grass, int foliage) {
        EnvironmentAttributeMap.Builder map = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(look.fog()))
                .set(EnvironmentAttributes.FOG_START_DISTANCE, look.fogStart())
                .set(EnvironmentAttributes.FOG_END_DISTANCE, look.fogEnd())
                .set(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.vector3fFromRGB24(look.waterFog()))
                .set(EnvironmentAttributes.WATER_FOG_END_DISTANCE, look.waterFogEnd());
        attributes.accept(map);
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        spawns.accept(mobs);
        context.register(key, new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.7F)
                .downfall(0.4F)
                .putAttributes(map)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(look.water())
                        .grassColorOverride(grass)
                        .foliageColorOverride(foliage)
                        .build())
                .mobSpawnSettings(mobs.build())
                .generationSettings(generation)
                .build());
    }

    private static List<AmbientParticle> particles(ParticleOptions particle, float probability) {
        return List.of(new AmbientParticle(particle, probability));
    }

    private static AmbientSounds loop(Holder<SoundEvent> loop, AmbientAdditionsSettings addition) {
        return new AmbientSounds(Optional.of(loop), Optional.empty(), addition == null ? List.of() : List.of(addition));
    }

    private static BackgroundMusic music(Holder<SoundEvent> sound) {
        return new BackgroundMusic(Optional.of(DimensionTypeData.music(sound, 6000, 18000)), Optional.empty(), Optional.empty());
    }
}
