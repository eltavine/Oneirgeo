package com.eltavine.oneirgeo.mixin;

import com.eltavine.oneirgeo.world.gen.OneirgeoChunkGenerator;
import com.mojang.serialization.Lifecycle;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Layered dimensions are the intended default, so worlds using them must not be flagged as experimental. */
@Mixin(WorldDimensions.class)
public abstract class WorldDimensionsMixin {
    @Inject(method = "checkStability", at = @At("HEAD"), cancellable = true)
    private static void oneirgeo$layeredIsStable(ResourceKey<LevelStem> key, LevelStem dimension, CallbackInfoReturnable<Lifecycle> cir) {
        if (dimension.generator() instanceof OneirgeoChunkGenerator) {
            cir.setReturnValue(Lifecycle.stable());
        }
    }
}
