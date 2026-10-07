package com.eltavine.oneirgeo.mixin.client;

import com.eltavine.oneirgeo.client.entity.FadedMobs;
import com.eltavine.oneirgeo.client.space.ClientGravity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.eltavine.oneirgeo.space.Gravity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Anyone standing on a ceiling is drawn standing on it, head down. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {
    @Shadow
    public abstract Identifier getTextureLocation(S state);

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void oneirgeo$rememberFlip(T entity, S state, float partialTicks, CallbackInfo ci) {
        state.setData(ClientGravity.FLIPPED, Gravity.isFlipped(entity));
        state.setData(FadedMobs.FADED, FadedMobs.isFaded(entity));
    }

    /** Animals in the dreams are drawn faded: grey and half transparent. */
    @WrapOperation(method = "submit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V"))
    private void oneirgeo$fade(SubmitNodeCollector collector, Model<?> model, Object renderState, PoseStack poseStack, RenderType renderType,
                               int light, int overlay, int tint, UvMapping uv, int outline, Operation<Void> original, @Local(argsOnly = true) S state) {
        if (Boolean.TRUE.equals(state.getData(FadedMobs.FADED))) {
            original.call(collector, model, renderState, poseStack, RenderTypes.entityTranslucentCull(this.getTextureLocation(state)), light, overlay,
                    ARGB.multiply(tint, FadedMobs.TINT), uv, outline);
        } else {
            original.call(collector, model, renderState, poseStack, renderType, light, overlay, tint, uv, outline);
        }
    }

    @Inject(method = "setupRotations", at = @At("TAIL"))
    private void oneirgeo$hangUpsideDown(S state, PoseStack poseStack, float bodyRot, float entityScale, CallbackInfo ci) {
        if (Boolean.TRUE.equals(state.getData(ClientGravity.FLIPPED))) {
            poseStack.translate(0.0F, state.boundingBoxHeight / entityScale, 0.0F);
            poseStack.rotate(Axis.ZP, (float) Math.PI);
        }
    }
}
