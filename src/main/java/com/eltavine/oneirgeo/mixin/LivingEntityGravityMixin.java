package com.eltavine.oneirgeo.mixin;

import com.eltavine.oneirgeo.space.Gravity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityGravityMixin {
    @Shadow
    protected abstract float getJumpPower();

    @ModifyExpressionValue(method = "travelInAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getEffectiveGravity()D"))
    private double oneirgeo$invertGravity(double gravity) {
        return Gravity.isFlipped((LivingEntity) (Object) this) ? -gravity : gravity;
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void oneirgeo$jumpDownwards(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!Gravity.isFlipped(self)) {
            return;
        }
        float jumpPower = this.getJumpPower();
        if (jumpPower > 1.0E-5F) {
            Vec3 movement = self.getDeltaMovement();
            self.setDeltaMovement(movement.x, Math.min(-jumpPower, movement.y), movement.z);
            if (self.isSprinting()) {
                float angle = self.getYRot() * Mth.DEG_TO_RAD;
                self.addDeltaMovement(new Vec3(-Mth.sin(angle) * 0.2, 0.0, Mth.cos(angle) * 0.2));
            }
            self.needsSync = true;
        }
        ci.cancel();
    }
}
