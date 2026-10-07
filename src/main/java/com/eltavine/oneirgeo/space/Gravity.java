package com.eltavine.oneirgeo.space;

import net.minecraft.world.entity.Entity;

/**
 * Which way is down. Inside flip zones gravity points to +Y: entities fall upwards, stand on the
 * undersides of blocks and jump towards -Y. Fall damage does not accumulate while flipped.
 */
public final class Gravity {
    private Gravity() {
    }

    /** Implemented on every entity by {@code EntityGravityMixin}; caches the answer per game tick. */
    public interface Holder {
        boolean oneirgeo$isFlipped();
    }

    public static boolean isFlipped(Entity entity) {
        return ((Holder) entity).oneirgeo$isFlipped();
    }

    /** Uncached test; prefer {@link #isFlipped}. */
    public static boolean compute(Entity entity) {
        return SpaceQuery.isFlipped(entity.level(), entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ());
    }
}
