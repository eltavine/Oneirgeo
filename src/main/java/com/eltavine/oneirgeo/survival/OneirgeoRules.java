package com.eltavine.oneirgeo.survival;

import com.eltavine.oneirgeo.Oneirgeo;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public final class OneirgeoRules {
    /** Ticks between two supply deliveries per player; 0 turns supplies off. */
    public static final GameRule<Integer> SUPPLY_INTERVAL = GameRuleBuilder.forInteger(6000).minValue(0)
            .category(GameRuleCategory.PLAYER).buildAndRegister(Oneirgeo.id("supply_interval"));

    /** Whether the hidden lucidity drifts at all. */
    public static final GameRule<Boolean> LUCIDITY = GameRuleBuilder.forBoolean(true)
            .category(GameRuleCategory.PLAYER).buildAndRegister(Oneirgeo.id("lucidity"));

    private OneirgeoRules() {
    }

    public static void init() {
    }
}
