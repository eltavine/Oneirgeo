package com.eltavine.oneirgeo.client.space;

import com.eltavine.oneirgeo.space.Gravity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/**
 * The local player's view while gravity is flipped: the camera rolls over in about half a second,
 * the eye moves to the other end of the body, and mouse and strafing are mirrored half way through.
 */
public final class ClientGravity {
    public static final RenderStateDataKey<Boolean> FLIPPED = RenderStateDataKey.create(() -> "oneirgeo:flipped");
    private static float progress;
    private static float previous;

    private ClientGravity() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            previous = progress;
            boolean flipped = client.player != null && Gravity.isFlipped(client.player);
            progress = Mth.approach(progress, flipped ? 1.0F : 0.0F, 0.12F);
        });
    }

    /** 0 upright, 1 upside down, eased and interpolated for the frame. */
    public static float amount(float partialTicks) {
        float t = Mth.lerp(partialTicks, previous, progress);
        return t * t * (3.0F - 2.0F * t);
    }

    public static float amount() {
        return amount(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
    }

    public static boolean controlsInverted() {
        return progress > 0.5F;
    }
}
