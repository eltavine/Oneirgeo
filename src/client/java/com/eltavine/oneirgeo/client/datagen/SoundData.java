package com.eltavine.oneirgeo.client.datagen;

import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricSoundsProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Every sound is a vanilla recording played back slower, higher or quieter. Nothing is
 * redistributed: the files are resolved from the player's own game assets.
 */
final class SoundData extends FabricSoundsProvider {
    SoundData(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    private static SoundTypeBuilder.RegistrationBuilder file(String path, float pitch, float volume) {
        return SoundTypeBuilder.RegistrationBuilder.ofFile(Identifier.withDefaultNamespace(path)).pitch(pitch).volume(volume);
    }

    private static SoundTypeBuilder.RegistrationBuilder stream(String path, float pitch, float volume) {
        return file(path, pitch, volume).stream(true);
    }

    private static SoundTypeBuilder event(Holder<SoundEvent> sound) {
        return SoundTypeBuilder.of(sound.value());
    }

    @Override
    protected void configure(HolderLookup.Provider registries, SoundExporter exporter) {
        exporter.add(OneirgeoSounds.AMBIENT_DUSK, event(OneirgeoSounds.AMBIENT_DUSK).sound(stream("ambient/nether/warped_forest/ambience", 0.52F, 0.22F)));
        exporter.add(OneirgeoSounds.AMBIENT_HIGH_WIND, event(OneirgeoSounds.AMBIENT_HIGH_WIND).sound(stream("ambient/nether/soulsand_valley/ambience", 1.1F, 0.18F)));
        exporter.add(OneirgeoSounds.AMBIENT_ROOMS, event(OneirgeoSounds.AMBIENT_ROOMS).sound(stream("ambient/nether/basalt_deltas/ambience", 0.5F, 0.2F)));
        SoundTypeBuilder roomsMood = event(OneirgeoSounds.AMBIENT_ROOMS_MOOD);
        for (int i : new int[]{1, 4, 7, 9, 13, 16, 19}) {
            roomsMood.sound(file("ambient/cave/cave" + i, 0.54F, 0.7F));
        }
        exporter.add(OneirgeoSounds.AMBIENT_ROOMS_MOOD, roomsMood);
        exporter.add(OneirgeoSounds.AMBIENT_FURNACE, event(OneirgeoSounds.AMBIENT_FURNACE).sound(stream("ambient/nether/nether_wastes/ambience", 0.5F, 0.32F)));
        SoundTypeBuilder furnaceMood = event(OneirgeoSounds.AMBIENT_FURNACE_MOOD);
        for (int i = 1; i <= 5; i++) {
            furnaceMood.sound(file("ambient/nether/nether_wastes/mood" + i, 0.58F, 0.6F));
        }
        exporter.add(OneirgeoSounds.AMBIENT_FURNACE_MOOD, furnaceMood);
        exporter.add(OneirgeoSounds.AMBIENT_HUM, event(OneirgeoSounds.AMBIENT_HUM).sound(file("block/beacon/ambient", 0.5F, 0.22F)));
        exporter.add(OneirgeoSounds.AMBIENT_VOID, event(OneirgeoSounds.AMBIENT_VOID).sound(stream("ambient/nether/soulsand_valley/ambience", 0.5F, 0.2F)));
        exporter.add(OneirgeoSounds.AMBIENT_SEA, event(OneirgeoSounds.AMBIENT_SEA).sound(stream("ambient/underwater/underwater_ambience", 0.58F, 0.22F)));
        exporter.add(OneirgeoSounds.AMBIENT_POOL, event(OneirgeoSounds.AMBIENT_POOL).sound(stream("ambient/underwater/underwater_ambience", 1.0F, 0.16F)));
        SoundTypeBuilder drip = event(OneirgeoSounds.AMBIENT_POOL_DRIP);
        for (int i = 1; i <= 8; i++) {
            drip.sound(file("block/pointed_dripstone/drip_water" + i, 0.66F, 0.5F));
        }
        exporter.add(OneirgeoSounds.AMBIENT_POOL_DRIP, drip);
        SoundTypeBuilder closed = event(OneirgeoSounds.AMBIENT_CLOSED);
        for (int i = 1; i <= 4; i++) {
            closed.sound(file("mob/warden/heartbeat_" + i, 0.85F, 0.35F));
        }
        exporter.add(OneirgeoSounds.AMBIENT_CLOSED, closed);
        exporter.add(OneirgeoSounds.AMBIENT_FOG, event(OneirgeoSounds.AMBIENT_FOG).sound(stream("ambient/nether/soulsand_valley/ambience", 0.5F, 0.24F)));
        SoundTypeBuilder pulse = event(OneirgeoSounds.AMBIENT_PULSE);
        for (int i = 1; i <= 4; i++) {
            pulse.sound(file("mob/warden/heartbeat_" + i, 0.55F, 0.45F));
        }
        exporter.add(OneirgeoSounds.AMBIENT_PULSE, pulse);
        SoundTypeBuilder current = event(OneirgeoSounds.AMBIENT_CURRENT);
        for (int i = 1; i <= 9; i++) {
            current.sound(file("block/conduit/short" + i, 0.55F, 0.25F));
        }
        exporter.add(OneirgeoSounds.AMBIENT_CURRENT, current);

        exporter.add(OneirgeoSounds.MUSIC_DREAM, event(OneirgeoSounds.MUSIC_DREAM)
                .sound(stream("music/game/a_familiar_room", 0.74F, 0.7F))
                .sound(stream("music/game/comforting_memories", 0.74F, 0.7F))
                .sound(stream("music/game/floating_dream", 0.72F, 0.7F))
                .sound(stream("music/game/komorebi", 0.74F, 0.7F))
                .sound(stream("music/menu/beginning_2", 0.72F, 0.7F)));
        exporter.add(OneirgeoSounds.MUSIC_DEEP, event(OneirgeoSounds.MUSIC_DEEP)
                .sound(stream("music/game/ancestry", 0.68F, 0.7F))
                .sound(stream("music/game/deeper", 0.68F, 0.7F)));
        exporter.add(OneirgeoSounds.MUSIC_FURNACE, event(OneirgeoSounds.MUSIC_FURNACE)
                .sound(stream("music/game/nether/concrete_halls", 0.7F, 0.7F))
                .sound(stream("music/game/nether/dead_voxel", 0.7F, 0.7F))
                .sound(stream("records/13", 0.77F, 0.55F)));
        exporter.add(OneirgeoSounds.MUSIC_VOID, event(OneirgeoSounds.MUSIC_VOID)
                .sound(stream("music/game/end/the_end", 0.63F, 0.7F))
                .sound(stream("records/11", 0.59F, 0.45F)));

        SoundTypeBuilder whisper = event(OneirgeoSounds.WHISPER);
        for (int i : new int[]{2, 6, 11, 13, 17}) {
            whisper.sound(file("ambient/cave/cave" + i, 1.55F, 0.45F));
        }
        exporter.add(OneirgeoSounds.WHISPER, whisper);
        exporter.add(OneirgeoSounds.SUPPLY, event(OneirgeoSounds.SUPPLY).sound(file("block/amethyst/shimmer", 0.6F, 0.6F)));
        exporter.add(OneirgeoSounds.PASSAGE, event(OneirgeoSounds.PASSAGE).sound(file("block/wooden_door/open1", 0.55F, 0.8F)).sound(file("block/wooden_door/open2", 0.5F, 0.8F)));
        exporter.add(OneirgeoSounds.WAKE, event(OneirgeoSounds.WAKE).sound(file("block/amethyst/resonate1", 0.7F, 0.8F)));
        exporter.add(OneirgeoSounds.HEAL, event(OneirgeoSounds.HEAL).sound(file("block/amethyst/resonate2", 0.5F, 0.25F)));
        exporter.add(OneirgeoSounds.ENTITY_FIGURE, event(OneirgeoSounds.ENTITY_FIGURE).sound(file("ambient/cave/cave4", 0.5F, 0.8F)));
        SoundTypeBuilder steps = event(OneirgeoSounds.ENTITY_STALKER);
        for (int i = 1; i <= 6; i++) {
            steps.sound(file("step/stone" + i, 0.7F, 0.6F));
        }
        exporter.add(OneirgeoSounds.ENTITY_STALKER, steps);
        exporter.add(OneirgeoSounds.TELEPHONE_RING, event(OneirgeoSounds.TELEPHONE_RING).sound(file("note/bell", 1.26F, 0.7F)));
        exporter.add(OneirgeoSounds.TELEPHONE_LINE, event(OneirgeoSounds.TELEPHONE_LINE).sound(file("ambient/weather/rain1", 1.7F, 0.35F)));
        exporter.add(OneirgeoSounds.TV_ON, event(OneirgeoSounds.TV_ON).sound(file("block/beacon/activate", 1.9F, 0.35F)));
        exporter.add(OneirgeoSounds.TAPE_STATIC, event(OneirgeoSounds.TAPE_STATIC).sound(file("ambient/weather/rain1", 1.9F, 0.45F)));
        exporter.add(OneirgeoSounds.CLOCK_TICK, event(OneirgeoSounds.CLOCK_TICK).sound(file("random/click", 0.55F, 0.5F)));
        exporter.add(OneirgeoSounds.MONITOR_BEEP, event(OneirgeoSounds.MONITOR_BEEP).sound(file("note/pling", 1.9F, 0.3F)));
        SoundTypeBuilder knock = event(OneirgeoSounds.KNOCK);
        for (int i = 1; i <= 4; i++) {
            knock.sound(file("dig/wood" + i, 0.6F, 0.9F));
        }
        exporter.add(OneirgeoSounds.KNOCK, knock);
        exporter.add(OneirgeoSounds.LIGHTS_OUT, event(OneirgeoSounds.LIGHTS_OUT).sound(file("block/beacon/deactivate", 0.6F, 0.6F)));
    }

    @Override
    public String getName() {
        return "Oneirgeo sounds";
    }
}
