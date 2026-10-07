package com.eltavine.oneirgeo.mixin.client;

import com.eltavine.oneirgeo.client.audio.Reverb;
import com.mojang.blaze3d.audio.Library;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Library.class)
public abstract class LibraryMixin {
    @Shadow
    private long currentDevice;

    @Inject(method = "init", at = @At("TAIL"))
    private void oneirgeo$openReverb(CallbackInfo ci) {
        Reverb.open(this.currentDevice);
    }

    @Inject(method = "cleanup()V", at = @At("HEAD"))
    private void oneirgeo$closeReverb(CallbackInfo ci) {
        Reverb.close();
    }
}
