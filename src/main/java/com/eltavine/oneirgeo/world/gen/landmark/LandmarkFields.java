package com.eltavine.oneirgeo.world.gen.landmark;

import com.eltavine.oneirgeo.Oneirgeo;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;

public final class LandmarkFields {
    private static final Map<Identifier, LandmarkField> FIELDS = new HashMap<>();

    static {
        FIELDS.put(Oneirgeo.id("none"), LandmarkField.NONE);
        FIELDS.put(Oneirgeo.id("overworld"), new OverworldLandmarks());
        FIELDS.put(Oneirgeo.id("nether"), new NetherLandmarks());
        FIELDS.put(Oneirgeo.id("end"), new EndLandmarks());
        FIELDS.put(Oneirgeo.id("mirror_sea"), new MirrorLandmarks());
    }

    private LandmarkFields() {
    }

    public static LandmarkField get(Identifier id) {
        return FIELDS.getOrDefault(id, LandmarkField.NONE);
    }
}
