package com.eltavine.oneirgeo.mixin.client;

import com.eltavine.oneirgeo.client.sky.MirrorSky;
import com.eltavine.oneirgeo.client.sky.WrongSky;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.MoonPhase;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
    @Unique
    private @Nullable GpuBuffer oneirgeo$eye;

    @Inject(method = "renderSunMoonAndStars", at = @At("TAIL"))
    private void oneirgeo$wrongSky(RenderPass renderPass, PoseStack poseStack, float sunAngle, float moonAngle, float starAngle,
                                   MoonPhase moonPhase, float rainBrightness, float starBrightness, CallbackInfo ci) {
        if (!WrongSky.active()) {
            return;
        }
        SkyRendererAccessor sky = (SkyRendererAccessor) this;
        if (this.oneirgeo$eye == null) {
            this.oneirgeo$eye = SkyRendererAccessor.oneirgeo$buildQuad("Oneirgeo eye", sky.oneirgeo$celestials().getSprite(WrongSky.EYE));
        }
        WrongSky.draw(sky, renderPass, this.oneirgeo$eye, starAngle, moonPhase, rainBrightness);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunMoonAndStars(Lcom/mojang/renderpearl/api/commands/RenderPass;Lcom/mojang/blaze3d/vertex/PoseStack;FFFLnet/minecraft/world/level/MoonPhase;FF)V"))
    private void oneirgeo$mirroredSky(GpuBufferSlice skyFog, SkyRenderState state, CallbackInfo ci, @Local RenderPass renderPass) {
        if (MirrorSky.active()) {
            MirrorSky.draw((SkyRendererAccessor) this, renderPass, state);
        }
    }

    @Inject(method = "shouldRenderDarkDisc", at = @At("HEAD"), cancellable = true)
    private void oneirgeo$noDarkDiscOverTheMirror(float partialTicks, ClientLevel level, CallbackInfoReturnable<Boolean> cir) {
        if (MirrorSky.active()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void oneirgeo$closeEye(CallbackInfo ci) {
        if (this.oneirgeo$eye != null) {
            this.oneirgeo$eye.close();
            this.oneirgeo$eye = null;
        }
    }
}
