package com.eltavine.oneirgeo.mixin;

import com.eltavine.oneirgeo.space.Gravity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Flipped gravity for every entity: inverted pull, ground on the underside of blocks, no fall damage. */
@Mixin(Entity.class)
public abstract class EntityGravityMixin implements Gravity.Holder {
    @Shadow
    public boolean verticalCollision;
    @Shadow
    public boolean verticalCollisionBelow;

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract void resetFallDistance();

    @Unique
    private long oneirgeo$flipTick = Long.MIN_VALUE;
    @Unique
    private boolean oneirgeo$flipped;

    @Override
    public boolean oneirgeo$isFlipped() {
        long time = this.level().getGameTime();
        if (time != this.oneirgeo$flipTick) {
            this.oneirgeo$flipTick = time;
            this.oneirgeo$flipped = Gravity.compute((Entity) (Object) this);
        }
        return this.oneirgeo$flipped;
    }

    @ModifyExpressionValue(method = "applyGravity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getGravity()D"))
    private double oneirgeo$invertGravity(double gravity) {
        return this.oneirgeo$isFlipped() ? -gravity : gravity;
    }

    @WrapOperation(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setOnGroundWithMovement(ZZLnet/minecraft/world/phys/Vec3;)V"))
    private void oneirgeo$groundAbove(Entity self, boolean onGround, boolean horizontalCollision, Vec3 movement, Operation<Void> original,
                                      @Local(argsOnly = true) Vec3 delta) {
        if (this.oneirgeo$isFlipped()) {
            boolean above = this.verticalCollision && delta.y > 0.0;
            this.verticalCollisionBelow = above;
            original.call(self, above, horizontalCollision, movement);
        } else {
            original.call(self, onGround, horizontalCollision, movement);
        }
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void oneirgeo$noFallWhileFlipped(CallbackInfo ci) {
        if (this.oneirgeo$isFlipped()) {
            this.resetFallDistance();
        }
    }
}
