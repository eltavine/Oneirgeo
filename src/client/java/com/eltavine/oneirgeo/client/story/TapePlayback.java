package com.eltavine.oneirgeo.client.story;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.client.config.OneirgeoConfig;
import com.eltavine.oneirgeo.network.RecordingPayload;
import com.eltavine.oneirgeo.util.Hash;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

/**
 * One of the father's recordings, played back on the camcorder screen: the picture goes dark and
 * snowy, and what the tape shows comes up line by line. The viewfinder reads PLAY and the tape's
 * date while it runs.
 */
public final class TapePlayback {
    private static final long LINE_MS = 3600L;
    private static final long NOISE_MS = 700L;
    private static @Nullable String tape;
    private static long started;
    private static int lines;

    private TapePlayback() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(RecordingPayload.TYPE, (payload, context) -> start(payload.tape()));
        HudElementRegistry.addLast(Oneirgeo.id("tape_playback"), TapePlayback::extract);
    }

    public static void start(String id) {
        int count = 0;
        while (Language.getInstance().has("oneirgeo.tape." + id + "." + count)) {
            count++;
        }
        tape = id;
        lines = count;
        started = System.currentTimeMillis();
    }

    private static long length() {
        return NOISE_MS * 2 + lines * LINE_MS;
    }

    public static boolean active() {
        return tape != null && System.currentTimeMillis() - started < length();
    }

    public static long elapsedMillis() {
        return System.currentTimeMillis() - started;
    }

    /** The date written on the tape, in the camcorder's format. */
    public static String date() {
        return tape == null ? "" : Component.translatable("oneirgeo.tape." + tape + ".date").getString();
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!active()) {
            tape = null;
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        long t = elapsedMillis();
        long total = length();
        float veil = Math.min(1.0F, Math.min(t, total - t) / (float) NOISE_MS);
        graphics.fill(0, 0, width, height, ARGB.color((int) (200 * veil), 0x05060A));
        boolean noisy = t < NOISE_MS || t > total - NOISE_MS;
        if (noisy) {
            snow(graphics, width, height, t, OneirgeoConfig.get().safeMode);
        }
        long into = t - NOISE_MS;
        if (into < 0 || into >= lines * LINE_MS) {
            return;
        }
        int index = (int) (into / LINE_MS);
        float phase = (into % LINE_MS) / (float) LINE_MS;
        float alpha = Math.min(1.0F, Math.min(phase / 0.15F, (1.0F - phase) / 0.15F));
        List<FormattedCharSequence> wrapped = font.split(Component.translatable("oneirgeo.tape." + tape + "." + index), (int) (width * 0.62F));
        int y = height / 2 - wrapped.size() * 6;
        for (FormattedCharSequence line : wrapped) {
            graphics.text(font, line, (width - font.width(line)) / 2, y, ARGB.color((int) (235 * alpha), 0xE8E4DA), false);
            y += 12;
        }
    }

    /** Tape snow: grey speckles that change every frame, or one still grey in photosensitive safe mode. */
    private static void snow(GuiGraphicsExtractor graphics, int width, int height, long t, boolean safe) {
        if (safe) {
            graphics.fill(0, 0, width, height, 0x40808080);
            return;
        }
        long frame = t / 40;
        for (int i = 0; i < 260; i++) {
            long h = Hash.of(frame, i, 0x5A0);
            int x = (int) Math.floorMod(h, (long) width);
            int y = (int) Math.floorMod(Hash.next(h, 1), (long) height);
            int grey = 90 + (int) Math.floorMod(Hash.next(h, 2), 140L);
            graphics.fill(x, y, x + 2 + (int) Math.floorMod(Hash.next(h, 3), 8L), y + 1, ARGB.color(150, grey, grey, grey));
        }
    }
}
