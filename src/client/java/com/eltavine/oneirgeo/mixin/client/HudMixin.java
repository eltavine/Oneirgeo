package com.eltavine.oneirgeo.mixin.client;

import com.eltavine.oneirgeo.client.fx.Camcorder;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The crosshair follows the real aim across the shaking camcorder picture. */
@Mixin(Hud.class)
public abstract class HudMixin {
    @Inject(method = "extractCrosshair", at = @At("HEAD"))
    private void oneirgeo$followAim(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        float[] offset = Camcorder.aimOffset(graphics.guiHeight());
        graphics.pose().pushMatrix();
        graphics.pose().translate(offset[0], offset[1]);
    }

    @Inject(method = "extractCrosshair", at = @At("RETURN"))
    private void oneirgeo$restore(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        graphics.pose().popMatrix();
    }
}
