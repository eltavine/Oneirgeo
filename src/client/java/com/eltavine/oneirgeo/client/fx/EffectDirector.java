package com.eltavine.oneirgeo.client.fx;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.client.config.OneirgeoConfig;
import com.eltavine.oneirgeo.registry.OneirgeoAttachments;
import com.eltavine.oneirgeo.space.SpaceQuery;
import com.eltavine.oneirgeo.survival.Lucidity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/**
 * Decides how strongly each post effect runs: a base look per scene biome, pushed further as the
 * player's hidden lucidity falls, then scaled by the client config and smoothed over time.
 */
public final class EffectDirector {
    /** bloom, vignette, exposure, chromatic, grain, pixelate, jpeg, lens */
    public static final int COUNT = 8;
    private static final float[] SMOOTHED = new float[COUNT];
    private static final float[] TARGET = new float[COUNT];
    private static final float[] TINT = {1.0F, 1.0F, 1.0F, 0.0F};
    private static final float[] TINT_TARGET = {1.0F, 1.0F, 1.0F, 0.0F};
    private static float warp;
    private static float lucidity = Lucidity.DEFAULT;
    private static boolean inTrap;
    private static final Map<String, Look> LOOKS = new HashMap<>();
    private static final Look NEUTRAL = new Look(0.25F, 0.3F, 0.1F, 0.03F, 0.1F, 0, 0, 0.03F, 0.0F, 1, 1, 1);

    /** Base look of a biome; saturation is an offset (negative desaturates). */
    record Look(float bloom, float vignette, float exposure, float chromatic, float grain, float pixelate, float jpeg, float lens,
                float saturation, float r, float g, float b) {
        float get(int i) {
            return switch (i) {
                case 0 -> this.bloom;
                case 1 -> this.vignette;
                case 2 -> this.exposure;
                case 3 -> this.chromatic;
                case 4 -> this.grain;
                case 5 -> this.pixelate;
                case 6 -> this.jpeg;
                default -> this.lens;
            };
        }
    }

    static {
        look("neural_fog", new Look(0.14F, 0.66F, -0.08F, 0.06F, 0.26F, 0, 0.02F, 0.08F, -0.5F, 1.03F, 0.95F, 0.88F));
        look("hanging_city", new Look(0.3F, 0.62F, 0.04F, 0.1F, 0.34F, 0, 0.05F, 0.08F, -0.2F, 1.06F, 0.94F, 0.9F));
        look("great_hearth", new Look(0.38F, 0.55F, 0.1F, 0.08F, 0.28F, 0, 0.02F, 0.06F, -0.12F, 1.08F, 0.95F, 0.88F));
        look("ash_plains", new Look(0.2F, 0.62F, 0.02F, 0.06F, 0.42F, 0, 0.04F, 0.05F, -0.52F, 1.0F, 0.98F, 0.96F));
        look("lava_sea", new Look(0.42F, 0.56F, 0.12F, 0.08F, 0.32F, 0, 0.02F, 0.06F, -0.14F, 1.06F, 0.94F, 0.88F));
        look("boiler_corridors", new Look(0.24F, 0.76F, 0.0F, 0.12F, 0.44F, 0.05F, 0.12F, 0.1F, -0.24F, 1.08F, 0.9F, 0.86F));
        look("void_geometry", new Look(0.32F, 0.62F, 0.02F, 0.16F, 0.24F, 0, 0.02F, 0.2F, -0.36F, 0.95F, 0.95F, 1.04F));
        look("star_cemetery", new Look(0.28F, 0.67F, 0.0F, 0.18F, 0.26F, 0, 0.04F, 0.22F, -0.4F, 0.95F, 0.96F, 1.04F));
        look("night_sea", new Look(0.36F, 0.72F, -0.03F, 0.14F, 0.28F, 0, 0.05F, 0.18F, -0.48F, 0.93F, 0.95F, 1.04F));
        look("mirror_sea", new Look(0.22F, 0.34F, 0.0F, 0.05F, 0.12F, 0, 0, 0.1F, -0.38F, 0.97F, 0.99F, 1.02F));
        look("poolrooms", new Look(0.36F, 0.4F, 0.14F, 0.08F, 0.18F, 0, 0.04F, 0.16F, -0.16F, 0.96F, 1.01F, 1.0F));
        look("backrooms", new Look(0.22F, 0.62F, 0.02F, 0.12F, 0.51F, 0.12F, 0.26F, 0.08F, -0.28F, 1.04F, 1.0F, 0.84F));
        look("closed_room", new Look(0.2F, 0.95F, 0.0F, 0.3F, 0.5F, 0.1F, 0.3F, 0.3F, -0.5F, 1.0F, 0.95F, 0.95F));
    }

    private EffectDirector() {
    }

    private static void look(String biome, Look look) {
        LOOKS.put(biome, look);
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(EffectDirector::tick);
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        OneirgeoConfig config = OneirgeoConfig.get();
        if (player == null || level == null) {
            Arrays.fill(TARGET, 0.0F);
        } else {
            BlockPos pos = BlockPos.containing(player.getEyePosition());
            Identifier biome = level.getBiome(pos).unwrapKey().map(k -> k.identifier()).orElse(null);
            Look look = biome != null && biome.getNamespace().equals(Oneirgeo.MOD_ID) ? LOOKS.getOrDefault(biome.getPath(), NEUTRAL) : null;
            lucidity = player.getAttachedOrElse(OneirgeoAttachments.LUCIDITY, Lucidity.DEFAULT);
            inTrap = SpaceQuery.isTrap(level, player.position());
            if (look == null) {
                Arrays.fill(TARGET, 0.0F);
                TINT_TARGET[0] = TINT_TARGET[1] = TINT_TARGET[2] = 1.0F;
                TINT_TARGET[3] = 0.0F;
            } else {
                float unease = 1.0F - lucidity;
                for (int i = 0; i < COUNT; i++) {
                    TARGET[i] = look.get(i);
                }
                TARGET[1] += unease * 0.25F;
                TARGET[3] += unease * 0.22F;
                TARGET[4] += unease * 0.3F;
                TARGET[5] += Math.max(0.0F, unease - 0.6F) * 0.3F;
                TARGET[6] += Math.max(0.0F, unease - 0.5F) * 0.5F;
                TARGET[7] += unease * 0.18F;
                TINT_TARGET[0] = look.r();
                TINT_TARGET[1] = look.g();
                TINT_TARGET[2] = look.b();
                TINT_TARGET[3] = look.saturation() - unease * 0.3F;
                if (inTrap) {
                    TARGET[1] = Math.max(TARGET[1], 0.95F);
                    TARGET[3] += 0.2F;
                }
            }
            float[] perEffect = {config.bloom, config.vignette, config.exposure, config.chromatic, config.grain, config.pixelate, config.jpeg, config.lens};
            boolean[] flickers = {false, false, false, true, true, false, true, true};
            for (int i = 0; i < COUNT; i++) {
                TARGET[i] = Math.min(1.0F, TARGET[i]) * config.scaled(perEffect[i], flickers[i]);
            }
        }
        float rate = config.safeMode ? 0.02F : 0.06F;
        for (int i = 0; i < COUNT; i++) {
            SMOOTHED[i] += (TARGET[i] - SMOOTHED[i]) * rate;
        }
        for (int i = 0; i < 4; i++) {
            TINT[i] += (TINT_TARGET[i] - TINT[i]) * rate;
        }
        float targetWarp = (1.0F - lucidity) * (1.0F - lucidity) * 0.8F + (inTrap ? 0.3F : 0.0F);
        targetWarp *= config.scaled(config.lens, true);
        warp += (targetWarp - warp) * rate;
    }

    public static float strength(int index) {
        return SMOOTHED[index];
    }

    public static float tint(int index) {
        return TINT[index];
    }

    public static float warp() {
        return warp;
    }

    public static float lucidity() {
        return lucidity;
    }

    public static boolean inTrap() {
        return inTrap;
    }

    public static boolean active() {
        for (float value : SMOOTHED) {
            if (value > 0.002F) {
                return true;
            }
        }
        return Math.abs(TINT[3]) > 0.01F || Math.abs(TINT[0] - 1.0F) > 0.01F;
    }
}
