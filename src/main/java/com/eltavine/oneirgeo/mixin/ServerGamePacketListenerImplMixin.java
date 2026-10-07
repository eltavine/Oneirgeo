package com.eltavine.oneirgeo.mixin;

import com.eltavine.oneirgeo.space.Gravity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Falling upwards through a flip shaft takes far longer than the anti-flight check allows. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;
    @Shadow
    private boolean clientIsFloating;

    @Inject(method = "handlePlayerPositionChange", at = @At("TAIL"))
    private void oneirgeo$fallingUpIsNotFlying(CallbackInfo ci) {
        if (Gravity.isFlipped(this.player)) {
            this.clientIsFloating = false;
        }
    }
}
