package com.eltavine.oneirgeo.mixin;

import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Dying in the dream costs nothing: keep_inventory is on by default, so new worlds start with it and
 * can still turn it off. Worlds save every rule, so existing ones keep the value they had.
 */
@Mixin(GameRules.class)
public abstract class GameRulesMixin {
    @ModifyArg(method = "<clinit>", index = 2, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/gamerules/GameRules;registerBoolean(Ljava/lang/String;Lnet/minecraft/world/level/gamerules/GameRuleCategory;Z)Lnet/minecraft/world/level/gamerules/GameRule;"))
    private static boolean oneirgeo$keepInventoryByDefault(String id, GameRuleCategory category, boolean defaultValue) {
        return defaultValue || id.equals("keep_inventory");
    }
}
