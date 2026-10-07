package com.eltavine.oneirgeo.client.fx;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.client.config.OneirgeoConfig;
import com.eltavine.oneirgeo.client.space.ClientGravity;
import com.eltavine.oneirgeo.client.story.TapePlayback;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

/**
 * Everything is seen through an old handheld camcorder: the picture shakes in the operator's hands,
 * the tape look is laid over it by the dream post chain, and the viewfinder shows a blinking REC,
 * the running tape counter, the battery and the date.
 */
public final class Camcorder {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM. dd yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH);
    private static final int RED = 0xFFE0261E;
    private static final int WHITE = 0xFFF2F0EA;
    private static long recordingSince = System.currentTimeMillis();
    private static float motion;
    private static final float[] applied = new float[3];

    private Camcorder() {
    }

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> recordingSince = System.currentTimeMillis());
        HudElementRegistry.addLast(Oneirgeo.id("camcorder"), Camcorder::extract);
    }

    public static boolean active() {
        OneirgeoConfig config = OneirgeoConfig.get();
        Minecraft client = Minecraft.getInstance();
        return config.effects && config.camcorder && client.level != null && client.player != null;
    }

    /** Overall strength of the tape look for the post chain: 1 while filming, 0 otherwise. */
    public static float strength() {
        return active() ? OneirgeoConfig.get().intensity : 0.0F;
    }

    private static float shakeScale() {
        OneirgeoConfig config = OneirgeoConfig.get();
        return config.shake * (config.safeMode ? 0.15F : 1.0F);
    }

    /**
     * Hand-held shake in radians for yaw, pitch and roll: slow incommensurate swings of tired arms,
     * a fast tremor of the hands, and a jolt on every step while the operator walks.
     */
    public static float[] shake() {
        if (!active()) {
            applied[0] = applied[1] = applied[2] = 0.0F;
            return applied;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        float speed = player == null ? 0.0F : (float) player.getDeltaMovement().horizontalDistance();
        motion += (Math.min(1.0F, speed * 4.0F) - motion) * 0.05F;
        double t = (System.nanoTime() / 1.0E9) % 10000.0;
        float drift = shakeScale() * (1.0F + motion * 0.8F);
        float yaw = (float) (0.9 * Math.sin(t * 1.31) + 0.5 * Math.sin(t * 2.93 + 1.7) + 0.25 * Math.sin(t * 7.1 + 0.3)) * 2.4F * drift;
        float pitch = (float) (0.8 * Math.sin(t * 1.07 + 2.1) + 0.5 * Math.sin(t * 3.41 + 0.6) + 0.3 * Math.sin(t * 8.3 + 1.1)) * 1.8F * drift;
        float roll = (float) (0.9 * Math.sin(t * 0.83 + 0.9) + 0.4 * Math.sin(t * 2.21 + 2.5) + 0.2 * Math.sin(t * 6.7)) * 2.8F * drift;
        float tremor = shakeScale() * 0.35F;
        yaw += (float) (Math.sin(t * 11.3) + 0.6 * Math.sin(t * 13.7 + 1.3)) * tremor;
        pitch += (float) (Math.sin(t * 12.1 + 0.7) + 0.5 * Math.sin(t * 15.9)) * tremor;
        float step = shakeScale() * motion;
        double stride = t * 6.5;
        pitch += (float) (Math.abs(Math.sin(stride)) - 0.63) * 4.0F * step;
        roll += (float) Math.sin(stride) * 2.5F * step;
        yaw += (float) Math.sin(stride + 0.8) * 1.2F * step;
        float radians = (float) Math.toRadians(1.0);
        applied[0] = yaw * radians;
        applied[1] = pitch * radians;
        applied[2] = roll * radians;
        return applied;
    }

    /**
     * Where the player's real aim lands on the shaken picture, in GUI pixels from the centre, so the
     * crosshair can follow it.
     */
    public static float[] aimOffset(int guiHeight) {
        float yaw = applied[0];
        float pitch = applied[1];
        float roll = applied[2] + (float) Math.PI * ClientGravity.amount();
        double x = Math.sin(yaw);
        double y = -Math.cos(yaw) * Math.sin(pitch);
        double z = -Math.cos(yaw) * Math.cos(pitch);
        if (z > -1.0E-3 || (yaw == 0.0F && pitch == 0.0F)) {
            return new float[2];
        }
        double right = x * Math.cos(roll) + y * Math.sin(roll);
        double up = -x * Math.sin(roll) + y * Math.cos(roll);
        double fov = Math.toRadians(Minecraft.getInstance().gameRenderer.mainCamera().getFov());
        double focal = guiHeight / 2.0 / Math.tan(fov / 2.0);
        return new float[]{(float) (right / -z * focal), (float) (-up / -z * focal)};
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (!active() || client.gui.hud.isHidden()) {
            return;
        }
        Font font = client.font;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int margin = 14;
        corners(graphics, margin, width, height);

        long now = System.currentTimeMillis();
        boolean playing = TapePlayback.active();
        boolean lit = OneirgeoConfig.get().safeMode || (now / 600) % 2 == 0;
        int x = margin + 10;
        int y = margin + 8;
        if (playing) {
            graphics.fill(x, y, x + 2, y + 7, WHITE);
            graphics.fill(x + 2, y + 1, x + 4, y + 6, WHITE);
            graphics.fill(x + 4, y + 2, x + 6, y + 5, WHITE);
            graphics.text(font, "PLAY", x + 10, y, WHITE, false);
        } else if (lit) {
            graphics.fill(x, y + 1, x + 7, y + 6, RED);
            graphics.fill(x + 1, y, x + 6, y + 7, RED);
            graphics.text(font, "REC", x + 10, y, RED, false);
        }
        long seconds = (playing ? TapePlayback.elapsedMillis() : now - recordingSince) / 1000;
        String counter = String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60);
        graphics.text(font, counter, x + (playing ? 40 : 34), y, WHITE, false);

        int bx = width - margin - 34;
        graphics.outline(bx, y, 22, 9, WHITE);
        graphics.fill(bx + 22, y + 3, bx + 24, y + 6, WHITE);
        for (int i = 0; i < 2; i++) {
            graphics.fill(bx + 2 + i * 7, y + 2, bx + 7 + i * 7, y + 7, WHITE);
        }
        graphics.text(font, "SP", margin + 10, height - margin - 18, WHITE, false);
        LocalDateTime date = LocalDateTime.now();
        String day = playing ? TapePlayback.date() : date.format(DATE).toUpperCase(Locale.ROOT);
        if (!playing) {
            String time = date.format(TIME);
            graphics.text(font, time, width - margin - 10 - font.width(time), height - margin - 28, WHITE, false);
        }
        graphics.text(font, day, width - margin - 10 - font.width(day), height - margin - 18, WHITE, false);
    }

    /** The four bracket corners of a viewfinder. */
    private static void corners(GuiGraphicsExtractor graphics, int m, int w, int h) {
        int len = 14;
        graphics.fill(m, m, m + len, m + 1, WHITE);
        graphics.fill(m, m, m + 1, m + len, WHITE);
        graphics.fill(w - m - len, m, w - m, m + 1, WHITE);
        graphics.fill(w - m - 1, m, w - m, m + len, WHITE);
        graphics.fill(m, h - m - 1, m + len, h - m, WHITE);
        graphics.fill(m, h - m - len, m + 1, h - m, WHITE);
        graphics.fill(w - m - len, h - m - 1, w - m, h - m, WHITE);
        graphics.fill(w - m - 1, h - m - len, w - m, h - m, WHITE);
    }
}
