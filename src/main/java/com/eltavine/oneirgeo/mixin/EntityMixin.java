package com.eltavine.oneirgeo.mixin;

import com.eltavine.oneirgeo.space.ServerSpace;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Seams carry mobs and items as well as players; players are handled by client prediction. */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Unique
    private Vec3 oneirgeo$moveStart = Vec3.ZERO;

    @Inject(method = "move", at = @At("HEAD"))
    private void oneirgeo$beforeMove(MoverType moverType, Vec3 delta, CallbackInfo ci) {
        this.oneirgeo$moveStart = ((Entity) (Object) this).position();
    }

    @Inject(method = "move", at = @At("TAIL"))
    private void oneirgeo$afterMove(MoverType moverType, Vec3 delta, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide()) {
            ServerSpace.afterMove(self, this.oneirgeo$moveStart);
        }
    }
}
