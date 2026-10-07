package com.eltavine.oneirgeo.world.gen.scene;

import com.eltavine.oneirgeo.world.gen.DimensionLayout;
import com.eltavine.oneirgeo.world.gen.LayoutSampler;

/** What a scene needs to answer position queries without touching a chunk. */
public record SceneInfo(long seed, DimensionLayout.Layer layer, int layerIndex, LayoutSampler.Region region) {
    public int minY() {
        return this.layer.minY();
    }

    public int maxY() {
        return this.layer.maxY();
    }
}
