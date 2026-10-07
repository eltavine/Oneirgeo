package com.eltavine.oneirgeo.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** Every footstep, of players, mobs and anything else, is played at 240% of its usual volume. */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Unique
    private static final float STEP_VOLUME = 2.4F;

    @ModifyExpressionValue(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SoundInstance;getVolume()F"))
    private float oneirgeo$loudStepsOnPlay(float volume, @Local(argsOnly = true) SoundInstance instance) {
        return oneirgeo$scale(instance, volume);
    }

    @ModifyExpressionValue(method = "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SoundInstance;getVolume()F"))
    private float oneirgeo$loudStepsOnUpdate(float volume, @Local(argsOnly = true) SoundInstance instance) {
        return oneirgeo$scale(instance, volume);
    }

    @Unique
    private static float oneirgeo$scale(SoundInstance instance, float volume) {
        return instance.getIdentifier().getPath().endsWith(".step") ? volume * STEP_VOLUME : volume;
    }
}
