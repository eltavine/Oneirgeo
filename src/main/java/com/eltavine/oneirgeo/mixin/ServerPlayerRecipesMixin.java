package com.eltavine.oneirgeo.mixin;

import java.util.Collection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Recipes are never unlocked, so the recipe book stays empty and never interrupts; crafting still works by hand. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerRecipesMixin {
    @Inject(method = "awardRecipes", at = @At("HEAD"), cancellable = true)
    private void oneirgeo$neverUnlock(Collection<RecipeHolder<?>> recipes, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }
}
