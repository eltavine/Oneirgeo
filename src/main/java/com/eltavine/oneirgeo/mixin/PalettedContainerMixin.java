package com.eltavine.oneirgeo.mixin;

import net.minecraft.util.ThreadingDetector;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Every section of a 4064-block-tall chunk holds two paletted containers, and every container its own
 * threading detector with a semaphore and a lock: about 250 bytes of bookkeeping per section, hundreds
 * of megabytes across the loaded chunks. The detector only reports misuse and never guards anything,
 * so all containers share one and its checks are skipped.
 */
@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin {
    private static final ThreadingDetector ONEIRGEO$SHARED = new ThreadingDetector("PalettedContainer");

    @Redirect(method = "<init>*", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/ThreadingDetector;"))
    private ThreadingDetector oneirgeo$sharedDetector(String name) {
        return ONEIRGEO$SHARED;
    }

    @Inject(method = "acquire", at = @At("HEAD"), cancellable = true)
    private void oneirgeo$skipAcquire(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "release", at = @At("HEAD"), cancellable = true)
    private void oneirgeo$skipRelease(CallbackInfo ci) {
        ci.cancel();
    }
}
