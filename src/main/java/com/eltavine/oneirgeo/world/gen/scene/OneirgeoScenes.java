package com.eltavine.oneirgeo.world.gen.scene;

import com.eltavine.oneirgeo.world.gen.scene.dream.BackroomsScene;
import com.eltavine.oneirgeo.world.gen.scene.dream.MirrorSeaScene;
import com.eltavine.oneirgeo.world.gen.scene.dream.PoolroomsScene;
import com.eltavine.oneirgeo.world.gen.scene.end.NightSeaScene;
import com.eltavine.oneirgeo.world.gen.scene.end.StarCemeteryScene;
import com.eltavine.oneirgeo.world.gen.scene.end.VoidGeometryScene;
import com.eltavine.oneirgeo.world.gen.scene.nether.AshPlainsScene;
import com.eltavine.oneirgeo.world.gen.scene.nether.BoilerCorridorsScene;
import com.eltavine.oneirgeo.world.gen.scene.nether.GreatHearthScene;
import com.eltavine.oneirgeo.world.gen.scene.nether.HangingCityScene;
import com.eltavine.oneirgeo.world.gen.scene.nether.LavaSeaScene;
import com.eltavine.oneirgeo.world.gen.scene.neural.NeuralWebScene;

/** Every scene id that dimension layouts may reference. */
public final class OneirgeoScenes {
    private OneirgeoScenes() {
    }

    public static void init() {
        Scenes.register("neural_web", new NeuralWebScene());

        Scenes.register("hanging_city", new HangingCityScene());
        Scenes.register("great_hearth", new GreatHearthScene());
        Scenes.register("ash_plains", new AshPlainsScene());
        Scenes.register("lava_sea", new LavaSeaScene());
        Scenes.register("boiler_corridors", new BoilerCorridorsScene());

        Scenes.register("void_geometry", new VoidGeometryScene());
        Scenes.register("star_cemetery", new StarCemeteryScene());
        Scenes.register("night_sea", new NightSeaScene());

        Scenes.register("mirror_sea", new MirrorSeaScene());
        Scenes.register("poolrooms", new PoolroomsScene());
        Scenes.register("backrooms", new BackroomsScene());
    }
}
