package com.eltavine.oneirgeo.client.sky;

import com.eltavine.oneirgeo.world.gen.landmark.Landmark;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.RandomSource;

/**
 * Now and then a figure hundreds of blocks tall stands far off in the fog, where you were not looking.
 * It is only ever seen from afar: walk towards it, or wait, and it is gone.
 */
public final class DistantGiants {
    private static final int COLOUR = 0xFF161412;
    private static final RandomSource RANDOM = RandomSource.create();
    private static List<Landmark> parts = List.of();
    private static int cooldown = 2400;
    private static int remaining;
    private static double startDistance;
    private static double giantX;
    private static double giantZ;

    private DistantGiants() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client.player));
    }

    public static List<Landmark> current() {
        return parts;
    }

    private static void tick(LocalPlayer player) {
        if (player == null || !player.level().dimensionTypeRegistration().unwrapKey()
                .map(k -> k.identifier().getNamespace().equals("oneirgeo")).orElse(false)) {
            parts = List.of();
            return;
        }
        if (!parts.isEmpty()) {
            double distance = Math.hypot(giantX - player.getX(), giantZ - player.getZ());
            if (--remaining <= 0 || distance < startDistance - 80.0) {
                parts = List.of();
                cooldown = 2400 + RANDOM.nextInt(3600);
            }
            return;
        }
        if (--cooldown > 0) {
            return;
        }
        double side = (RANDOM.nextBoolean() ? 1 : -1) * Math.toRadians(70 + RANDOM.nextInt(40));
        double yaw = Math.toRadians(player.getYRot()) + side;
        startDistance = 320 + RANDOM.nextInt(140);
        giantX = player.getX() - Math.sin(yaw) * startDistance;
        giantZ = player.getZ() + Math.cos(yaw) * startDistance;
        int height = 180 + RANDOM.nextInt(120);
        parts = figure((int) giantX, (int) player.getY() - 60, (int) giantZ, height);
        remaining = 600 + RANDOM.nextInt(1000);
    }

    /** Legs, body, arms hanging down and a head, as boxes for the silhouette renderer. */
    private static List<Landmark> figure(int x, int y, int z, int h) {
        List<Landmark> out = new ArrayList<>();
        int leg = Math.max(2, h / 20);
        out.add(new Landmark(Landmark.Shape.BOX, x - h * 9 / 100, y, z, leg, h * 45 / 100, leg, COLOUR, 0));
        out.add(new Landmark(Landmark.Shape.BOX, x + h * 9 / 100, y, z, leg, h * 45 / 100, leg, COLOUR, 0));
        out.add(new Landmark(Landmark.Shape.BOX, x, y + h * 45 / 100, z, h * 16 / 100, h * 35 / 100, h * 8 / 100, COLOUR, 0));
        int arm = Math.max(2, h * 45 / 1000);
        out.add(new Landmark(Landmark.Shape.BOX, x - h * 22 / 100, y + h * 42 / 100, z, arm, h * 36 / 100, arm, COLOUR, 0));
        out.add(new Landmark(Landmark.Shape.BOX, x + h * 22 / 100, y + h * 42 / 100, z, arm, h * 36 / 100, arm, COLOUR, 0));
        out.add(new Landmark(Landmark.Shape.BOX, x, y + h * 82 / 100, z, h * 7 / 100, h * 16 / 100, h * 7 / 100, COLOUR, 0));
        return List.copyOf(out);
    }
}
