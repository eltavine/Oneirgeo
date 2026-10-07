package com.eltavine.oneirgeo.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Nothing here is ever achieved: advancements are never awarded, so no toasts and no announcements. */
@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {
    @Inject(method = "award", at = @At("HEAD"), cancellable = true)
    private void oneirgeo$neverAward(AdvancementHolder holder, String criterion, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
