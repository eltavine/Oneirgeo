package com.eltavine.oneirgeo.client.entity;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;

/** Animals that wander into the dreams lose their colour and most of their substance. */
public final class FadedMobs {
    public static final RenderStateDataKey<Boolean> FADED = RenderStateDataKey.create(() -> "oneirgeo:faded");
    /** Grey, at a little over half opacity. */
    public static final int TINT = 0x96B4B4BC;

    private FadedMobs() {
    }

    public static boolean isFaded(LivingEntity entity) {
        return entity instanceof Animal && entity.level().dimensionTypeRegistration().unwrapKey()
                .map(key -> key.identifier().getNamespace().equals("oneirgeo")).orElse(false);
    }
}
