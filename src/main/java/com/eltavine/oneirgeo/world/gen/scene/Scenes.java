package com.eltavine.oneirgeo.world.gen.scene;

import com.eltavine.oneirgeo.Oneirgeo;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;

/** Code-side registry of scene implementations, referenced by id from dimension layouts. */
public final class Scenes {
    private static final Map<Identifier, Scene> SCENES = new LinkedHashMap<>();
    private static final Set<Identifier> WARNED = new HashSet<>();

    public static final Scene EMPTY = ctx -> {
    };

    private Scenes() {
    }

    public static Scene register(String name, Scene scene) {
        Identifier id = Oneirgeo.id(name);
        if (SCENES.putIfAbsent(id, scene) != null) {
            throw new IllegalStateException("Duplicate scene " + id);
        }
        return scene;
    }

    public static Scene get(Identifier id) {
        Scene scene = SCENES.get(id);
        if (scene == null) {
            synchronized (WARNED) {
                if (WARNED.add(id)) {
                    Oneirgeo.LOGGER.warn("Unknown scene {}, generating nothing for it", id);
                }
            }
            return EMPTY;
        }
        return scene;
    }

    public static Set<Identifier> ids() {
        return SCENES.keySet();
    }
}
