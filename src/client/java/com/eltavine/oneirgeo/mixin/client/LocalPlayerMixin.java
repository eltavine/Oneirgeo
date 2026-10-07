package com.eltavine.oneirgeo.mixin.client;

import com.eltavine.oneirgeo.client.space.ClientSeams;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "sendPosition", at = @At("HEAD"))
    private void oneirgeo$crossSeams(CallbackInfo ci) {
        ClientSeams.beforeSendPosition((LocalPlayer) (Object) this);
    }
}
