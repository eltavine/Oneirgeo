package com.eltavine.oneirgeo.client.sky;

import com.eltavine.oneirgeo.mixin.client.SkyRendererAccessor;
import com.eltavine.oneirgeo.world.OneirgeoDimensions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.Mth;
import org.joml.Matrix4fStack;

/**
 * Over the mirror sea the lower half of the sky is the upper half turned over: the noon sun shines
 * up from below, and the horizon is a seam between two skies.
 */
public final class MirrorSky {
    private MirrorSky() {
    }

    public static boolean active() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null && minecraft.level.dimensionTypeRegistration().is(OneirgeoDimensions.MIRROR_SEA_TYPE);
    }

    public static void draw(SkyRendererAccessor sky, RenderPass pass, SkyRenderState state) {
        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.rotateX(Mth.PI);
        sky.oneirgeo$renderSkyDisc(pass, state.skyColor);
        stack.popMatrix();
        PoseStack pose = new PoseStack();
        pose.rotate(Axis.XP, Mth.PI);
        sky.oneirgeo$renderSunMoonAndStars(pass, pose, state.sunAngle, state.moonAngle, state.starAngle, state.moonPhase,
                state.rainBrightness, state.starBrightness);
    }
}
