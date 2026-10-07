package com.eltavine.oneirgeo.mixin.client;

import com.eltavine.oneirgeo.client.fx.Camcorder;
import com.eltavine.oneirgeo.client.space.ClientGravity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    @Final
    private static Vector3fc FORWARDS;
    @Shadow
    @Final
    private static Vector3fc UP;
    @Shadow
    @Final
    private static Vector3fc LEFT;
    @Shadow
    @Final
    private Quaternionf rotation;
    @Shadow
    @Final
    private Vector3f forwards;
    @Shadow
    @Final
    private Vector3f up;
    @Shadow
    @Final
    private Vector3f left;
    @Shadow
    private Entity entity;
    @Shadow
    private float eyeHeight;

    /** Rolls the view over inside flip zones, and shakes it like a camcorder held in unsteady hands. */
    @Inject(method = "setRotation", at = @At("TAIL"))
    private void oneirgeo$roll(float yRot, float xRot, CallbackInfo ci) {
        if (this.entity != Minecraft.getInstance().player) {
            return;
        }
        float amount = ClientGravity.amount();
        float[] shake = Camcorder.shake();
        if (amount <= 0.0F && shake[0] == 0.0F && shake[1] == 0.0F && shake[2] == 0.0F) {
            return;
        }
        this.rotation.rotateY(shake[0]).rotateX(shake[1]).rotateZ(shake[2] + (float) Math.PI * amount);
        FORWARDS.rotate(this.rotation, this.forwards);
        UP.rotate(this.rotation, this.up);
        LEFT.rotate(this.rotation, this.left);
    }

    @WrapOperation(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"))
    private void oneirgeo$eyeAtTheOtherEnd(Camera camera, double x, double y, double z, Operation<Void> original) {
        if (this.entity == Minecraft.getInstance().player) {
            float amount = ClientGravity.amount();
            if (amount > 0.0F) {
                y += (this.entity.getBbHeight() - 2.0F * this.eyeHeight) * amount;
            }
        }
        original.call(camera, x, y, z);
    }
}
