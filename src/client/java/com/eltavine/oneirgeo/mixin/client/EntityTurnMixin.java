package com.eltavine.oneirgeo.mixin.client;

import com.eltavine.oneirgeo.client.space.ClientGravity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Upside down, the mouse would turn the world the wrong way round; mirror it. */
@Mixin(Entity.class)
public abstract class EntityTurnMixin {
    @ModifyVariable(method = "turn", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double oneirgeo$mirrorYaw(double xo) {
        return this.oneirgeo$inverted() ? -xo : xo;
    }

    @ModifyVariable(method = "turn", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double oneirgeo$mirrorPitch(double yo) {
        return this.oneirgeo$inverted() ? -yo : yo;
    }

    @Unique
    private boolean oneirgeo$inverted() {
        return (Object) this == Minecraft.getInstance().player && ClientGravity.controlsInverted();
    }
}
