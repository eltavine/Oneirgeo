package com.eltavine.oneirgeo.mixin.client;

import com.eltavine.oneirgeo.client.audio.Reverb;
import com.mojang.blaze3d.audio.Channel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Channel.class)
public abstract class ChannelMixin {
    @Shadow
    @Final
    private int source;
    @Unique
    private boolean oneirgeo$relative;

    @Inject(method = "setRelative", at = @At("HEAD"))
    private void oneirgeo$rememberRelative(boolean relative, CallbackInfo ci) {
        this.oneirgeo$relative = relative;
    }

    @Inject(method = "play", at = @At("HEAD"))
    private void oneirgeo$route(CallbackInfo ci) {
        Reverb.route(this.source, this.oneirgeo$relative);
    }
}
