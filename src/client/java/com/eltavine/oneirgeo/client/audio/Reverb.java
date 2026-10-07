package com.eltavine.oneirgeo.client.audio;

import com.eltavine.oneirgeo.client.config.OneirgeoConfig;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;

/**
 * One OpenAL EFX reverb shared by every positioned sound, tuned to where you stand: tiled halls ring
 * for seconds, the void swallows everything slowly, the fog is damp and close. Off outside the dreams.
 */
public final class Reverb {
    /** Standard EFX reverb parameters; {@code send} is the gain of the effect slot. */
    private record Preset(float decay, float density, float diffusion, float gain, float gainHf, float reflections, float late, float send) {
    }

    private static final Preset OFF = new Preset(0.1F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    private static final Map<String, Preset> PRESETS = Map.ofEntries(
            Map.entry("neural_fog", new Preset(3.2F, 0.9F, 0.8F, 0.32F, 0.35F, 0.15F, 1.3F, 0.55F)),
            Map.entry("poolrooms", new Preset(5.5F, 1.0F, 1.0F, 0.4F, 0.8F, 0.4F, 1.8F, 0.8F)),
            Map.entry("backrooms", new Preset(1.3F, 0.8F, 0.9F, 0.32F, 0.6F, 0.3F, 1.2F, 0.6F)),
            Map.entry("closed_room", new Preset(0.6F, 1.0F, 1.0F, 0.32F, 0.7F, 0.6F, 1.0F, 0.5F)),
            Map.entry("boiler_corridors", new Preset(2.2F, 1.0F, 0.9F, 0.32F, 0.5F, 0.35F, 1.4F, 0.6F)),
            Map.entry("hanging_city", new Preset(3.0F, 0.8F, 0.7F, 0.3F, 0.4F, 0.2F, 1.2F, 0.5F)),
            Map.entry("great_hearth", new Preset(4.5F, 0.6F, 0.6F, 0.3F, 0.3F, 0.1F, 1.4F, 0.5F)),
            Map.entry("ash_plains", new Preset(2.4F, 0.5F, 0.6F, 0.28F, 0.3F, 0.1F, 1.0F, 0.4F)),
            Map.entry("lava_sea", new Preset(2.6F, 0.5F, 0.6F, 0.28F, 0.3F, 0.1F, 1.0F, 0.4F)),
            Map.entry("star_cemetery", new Preset(7.0F, 0.3F, 0.5F, 0.25F, 0.2F, 0.05F, 1.6F, 0.5F)),
            Map.entry("void_geometry", new Preset(9.0F, 0.2F, 0.4F, 0.25F, 0.2F, 0.02F, 1.8F, 0.55F)),
            Map.entry("night_sea", new Preset(6.0F, 0.4F, 0.6F, 0.28F, 0.25F, 0.08F, 1.5F, 0.5F)),
            Map.entry("mirror_sea", new Preset(1.4F, 0.3F, 0.5F, 0.25F, 0.5F, 0.05F, 0.8F, 0.3F)));

    private static boolean supported;
    private static int effect;
    private static int slot;
    private static Preset applied = OFF;

    private Reverb() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(Reverb::tick);
    }

    /** Called by {@code LibraryMixin} once OpenAL is up. */
    public static void open(long device) {
        supported = ALC10.alcIsExtensionPresent(device, "ALC_EXT_EFX");
        if (!supported) {
            return;
        }
        AL10.alGetError();
        effect = EXTEfx.alGenEffects();
        EXTEfx.alEffecti(effect, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_REVERB);
        slot = EXTEfx.alGenAuxiliaryEffectSlots();
        applied = null;
        apply(OFF);
        if (AL10.alGetError() != AL10.AL_NO_ERROR) {
            supported = false;
        }
    }

    public static void close() {
        if (supported) {
            EXTEfx.alDeleteAuxiliaryEffectSlots(slot);
            EXTEfx.alDeleteEffects(effect);
            supported = false;
        }
    }

    /** Sends a source into the reverb, or out of it for sounds without a position such as music. */
    public static void route(int source, boolean relative) {
        if (supported) {
            AL11.alSource3i(source, EXTEfx.AL_AUXILIARY_SEND_FILTER, relative ? EXTEfx.AL_EFFECTSLOT_NULL : slot, 0, EXTEfx.AL_FILTER_NULL);
        }
    }

    private static void tick(Minecraft client) {
        if (!supported || client.level == null || client.player == null) {
            return;
        }
        Preset target = OFF;
        if (OneirgeoConfig.get().reverb) {
            BlockPos at = BlockPos.containing(client.gameRenderer.mainCamera().position());
            String biome = client.level.getBiome(at).unwrapKey().map(key -> key.identifier().getPath()).orElse("");
            target = PRESETS.getOrDefault(biome, OFF);
        }
        if (target != applied) {
            apply(target);
        }
    }

    private static void apply(Preset p) {
        applied = p;
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DECAY_TIME, Math.max(0.1F, p.decay()));
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DENSITY, p.density());
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_DIFFUSION, p.diffusion());
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_GAIN, p.gain());
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_GAINHF, p.gainHf());
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_REFLECTIONS_GAIN, p.reflections());
        EXTEfx.alEffectf(effect, EXTEfx.AL_REVERB_LATE_REVERB_GAIN, p.late());
        EXTEfx.alAuxiliaryEffectSloti(slot, EXTEfx.AL_EFFECTSLOT_EFFECT, effect);
        EXTEfx.alAuxiliaryEffectSlotf(slot, EXTEfx.AL_EFFECTSLOT_GAIN, p.send());
    }
}
