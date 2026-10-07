package com.eltavine.oneirgeo.mixin.client;

import com.eltavine.oneirgeo.client.space.ClientGravity;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Upside down, left on screen is right in the world; strafing follows the screen. */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
    @Inject(method = "tick", at = @At("TAIL"))
    private void oneirgeo$mirrorStrafe(CallbackInfo ci) {
        if (ClientGravity.controlsInverted()) {
            this.moveVector = new Vec2(-this.moveVector.x, this.moveVector.y);
        }
    }
}
