package com.eltavine.oneirgeo.client.sky;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.client.config.OneirgeoConfig;
import com.eltavine.oneirgeo.mixin.client.SkyRendererAccessor;
import com.eltavine.oneirgeo.world.OneirgeoDimensions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.MoonPhase;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;

/** The End's sky is wrong: three more moons that keep their own time, and an eye that blinks. */
public final class WrongSky {
    public static final Identifier EYE = Oneirgeo.id("eye");
    private static final float BLINK_PERIOD = 11.0F;
    private static final float BLINK_LENGTH = 0.28F;

    private WrongSky() {
    }

    public static boolean active() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null && OneirgeoConfig.get().wrongSky
                && minecraft.level.dimensionTypeRegistration().is(OneirgeoDimensions.END_TYPE);
    }

    public static void draw(SkyRendererAccessor sky, RenderPass pass, GpuBuffer eye, float starAngle, MoonPhase phase, float brightness) {
        int index = phase.index();
        moon(sky, pass, 28.0F, starAngle * 0.5F + 1.1F, (index + 3) % 8, 14.0F, brightness);
        moon(sky, pass, 205.0F, -starAngle * 0.8F + 2.3F, (index + 5) % 8, 9.0F, brightness);
        moon(sky, pass, 290.0F, starAngle * 1.3F + 0.4F, (index + 6) % 8, 5.0F, brightness * 0.8F);

        float seconds = (Util.getMillis() % 3_600_000L) / 1000.0F;
        float t = seconds % BLINK_PERIOD;
        float open = t < BLINK_LENGTH ? Math.max(0.04F, Math.abs(Mth.cos(t / BLINK_LENGTH * Mth.PI))) : 1.0F;
        PoseStack pose = new PoseStack();
        pose.rotateDegrees(Axis.YP, 140.0F);
        pose.rotateDegrees(Axis.XP, 52.0F);
        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.mul(pose.last().pose());
        stack.translate(0.0F, 100.0F, 0.0F);
        stack.scale(64.0F, 1.0F, 64.0F * open);
        Matrix4f matrix = new Matrix4f(stack);
        stack.popMatrix();
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(matrix, new Vector4f(1.0F, 1.0F, 1.0F, 0.9F * brightness));
        sky.oneirgeo$draw(() -> "Oneirgeo eye", pass, transforms, sky.oneirgeo$quadIndices().getBuffer(6), eye, 0);
    }

    private static void moon(SkyRendererAccessor sky, RenderPass pass, float yaw, float angle, int phase, float size, float brightness) {
        PoseStack pose = new PoseStack();
        pose.rotateDegrees(Axis.YP, yaw);
        pose.rotate(Axis.XP, angle);
        Matrix4f matrix = sky.oneirgeo$transform(pose, 100.0F, size);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(matrix, new Vector4f(1.0F, 1.0F, 1.0F, brightness));
        sky.oneirgeo$draw(() -> "Oneirgeo moon", pass, transforms, sky.oneirgeo$quadIndices().getBuffer(6), sky.oneirgeo$moons(), phase * 4);
    }
}
