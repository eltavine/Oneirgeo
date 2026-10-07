package com.eltavine.oneirgeo.client.fx;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.client.config.OneirgeoConfig;
import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Words that appear on the screen as if written over a photograph: "you have been here before".
 * Rare in daylight, more frequent as lucidity falls, constant in closed rooms. Lines come from the
 * language files, so they follow the game language.
 */
public final class ScreenText {
    public static final int LINES = 24;
    public static final int TRAP_LINES = 4;
    private static final RandomSource RANDOM = RandomSource.create();

    private static Component text;
    private static int age;
    private static int duration;
    private static float anchorX;
    private static float anchorY;
    private static float scale;

    private ScreenText() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(ScreenText::tick);
        HudElementRegistry.addLast(Oneirgeo.id("screen_text"), ScreenText::extract);
    }

    private static void tick(Minecraft client) {
        if (text != null) {
            if (++age >= duration) {
                text = null;
            }
            return;
        }
        OneirgeoConfig config = OneirgeoConfig.get();
        if (client.player == null || client.isPaused() || !config.screenText || !EffectDirector.active()) {
            return;
        }
        float unease = 1.0F - EffectDirector.lucidity();
        float chance = 0.0006F * (1.0F + 4.0F * unease) * (EffectDirector.inTrap() ? 6.0F : 1.0F) * (config.safeMode ? 0.5F : 1.0F);
        if (RANDOM.nextFloat() > chance) {
            return;
        }
        boolean trap = EffectDirector.inTrap() && RANDOM.nextBoolean();
        String key = trap ? "oneirgeo.whisper.trap." + RANDOM.nextInt(TRAP_LINES) : "oneirgeo.whisper." + RANDOM.nextInt(LINES);
        show(Component.translatable(key), config.safeMode);
        client.getSoundManager().play(SimpleSoundInstance.forUI(OneirgeoSounds.WHISPER.value(), 1.0F, 0.35F));
    }

    public static void show(Component line, boolean calm) {
        text = line;
        age = 0;
        duration = 70 + RANDOM.nextInt(50);
        if (calm) {
            anchorX = 0.5F;
            anchorY = 0.62F;
            scale = 1.5F;
        } else {
            anchorX = 0.25F + RANDOM.nextFloat() * 0.5F;
            anchorY = 0.2F + RANDOM.nextFloat() * 0.6F;
            scale = 1.2F + RANDOM.nextFloat() * 1.4F;
        }
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Component line = text;
        if (line == null) {
            return;
        }
        float t = age + deltaTracker.getGameTimeDeltaPartialTick(false);
        float fadeIn = Mth.clamp(t / 12.0F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((duration - t) / 20.0F, 0.0F, 1.0F);
        float alpha = Math.min(fadeIn, fadeOut) * 0.85F;
        if (alpha <= 0.02F) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        int width = font.width(line);
        graphics.pose().pushMatrix();
        graphics.pose().translate(graphics.guiWidth() * anchorX, graphics.guiHeight() * anchorY);
        graphics.pose().scale(scale, scale);
        graphics.text(font, line, -width / 2, -font.lineHeight / 2, ARGB.color(alpha, 0xFFF4F0E8), false);
        graphics.pose().popMatrix();
    }
}
