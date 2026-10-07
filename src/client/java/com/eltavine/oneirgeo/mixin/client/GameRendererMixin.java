package com.eltavine.oneirgeo.mixin.client;

import com.eltavine.oneirgeo.client.fx.DreamPost;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "update", at = @At("TAIL"))
    private void oneirgeo$requestDream(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (DreamPost.wanted()) {
            ((GameRenderer) (Object) this).getRequestedPostEffects().add(DreamPost.ID);
        }
    }

    @Inject(method = "applyPostEffects", at = @At("HEAD"))
    private void oneirgeo$beforePost(CallbackInfo ci) {
        DreamPost.beforeApply();
    }

    @Inject(method = "applyPostEffects", at = @At("TAIL"))
    private void oneirgeo$afterPost(CallbackInfo ci) {
        DreamPost.afterApply();
    }
}
