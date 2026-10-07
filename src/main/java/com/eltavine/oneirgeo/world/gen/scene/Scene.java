package com.eltavine.oneirgeo.world.gen.scene;

import org.jspecify.annotations.Nullable;

/**
 * One abstract landscape. Implementations must be pure functions of the seed and world coordinates:
 * a chunk only ever writes its own blocks, so shapes crossing chunk borders are recomputed per chunk.
 */
public interface Scene {
    /** Writes terrain for the owned columns of the chunk, clipped to the layer. */
    void generate(SceneContext ctx);

    /** Places blocks that need block entities (chests, beds, portals, signs) during the features step. */
    default void decorate(DecorationContext ctx) {
    }

    /** Topmost walkable block of this scene at a column, or {@code Integer.MIN_VALUE} for none. */
    default int surfaceY(SceneInfo info, int x, int z) {
        return Integer.MIN_VALUE;
    }

    /** Named biome variant for a position (looked up in the scene entry), or null for the base biome. */
    default @Nullable String biomeVariant(SceneInfo info, int x, int y, int z) {
        return null;
    }
}
