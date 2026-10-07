package com.eltavine.oneirgeo.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import java.util.function.Supplier;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.MoonPhase;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SkyRenderer.class)
public interface SkyRendererAccessor {
    @Accessor("celestialsAtlas")
    TextureAtlas oneirgeo$celestials();

    @Accessor("moonBuffer")
    GpuBuffer oneirgeo$moons();

    @Accessor("quadIndices")
    RenderSystem.AutoStorageIndexBuffer oneirgeo$quadIndices();

    @Invoker("applyCelestialBodyTransform")
    Matrix4f oneirgeo$transform(PoseStack poseStack, float height, float scale);

    @Invoker("drawCelestialBody")
    void oneirgeo$draw(Supplier<String> label, RenderPass renderPass, GpuBufferSlice dynamicTransforms, GpuBuffer indexBuffer,
                       GpuBuffer vertexBuffer, int baseVertex);

    @Invoker("renderSkyDisc")
    void oneirgeo$renderSkyDisc(RenderPass renderPass, Vector3fc skyColor);

    @Invoker("renderSunMoonAndStars")
    void oneirgeo$renderSunMoonAndStars(RenderPass renderPass, PoseStack poseStack, float sunAngle, float moonAngle, float starAngle,
                                        MoonPhase moonPhase, float rainBrightness, float starBrightness);

    @Invoker("buildCelestialQuad")
    static GpuBuffer oneirgeo$buildQuad(String name, TextureAtlasSprite sprite) {
        throw new AssertionError();
    }
}
