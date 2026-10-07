package com.eltavine.oneirgeo;

import com.eltavine.oneirgeo.command.OneirgeoCommands;
import com.eltavine.oneirgeo.dev.SelfTest;
import com.eltavine.oneirgeo.entity.Apparitions;
import com.eltavine.oneirgeo.entity.OneirgeoEntities;
import com.eltavine.oneirgeo.registry.OneirgeoAttachments;
import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.registry.OneirgeoComponents;
import com.eltavine.oneirgeo.registry.OneirgeoItems;
import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import com.eltavine.oneirgeo.space.ServerSpace;
import com.eltavine.oneirgeo.story.ChapterOfDimensionFunction;
import com.eltavine.oneirgeo.story.Story;
import com.eltavine.oneirgeo.survival.Lucidity;
import com.eltavine.oneirgeo.survival.OneirgeoRules;
import com.eltavine.oneirgeo.survival.Supplies;
import com.eltavine.oneirgeo.survival.VerticalTravel;
import com.eltavine.oneirgeo.world.Dreams;
import com.eltavine.oneirgeo.world.EndTime;
import com.eltavine.oneirgeo.world.LandmarkSync;
import com.eltavine.oneirgeo.world.LootAdditions;
import com.eltavine.oneirgeo.world.gen.OneirgeoWorldgen;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oneirgeo implements ModInitializer {
    public static final String MOD_ID = "oneirgeo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        OneirgeoSounds.init();
        OneirgeoComponents.init();
        ChapterOfDimensionFunction.init();
        OneirgeoBlocks.init();
        OneirgeoItems.init();
        OneirgeoEntities.init();
        OneirgeoAttachments.init();
        OneirgeoWorldgen.init();
        ServerSpace.init();
        OneirgeoRules.init();
        Supplies.init();
        Lucidity.init();
        VerticalTravel.init();
        LandmarkSync.init();
        EndTime.init();
        Dreams.init();
        LootAdditions.init();
        Apparitions.init();
        Story.init();
        com.eltavine.oneirgeo.world.Mysteries.init();
        com.eltavine.oneirgeo.world.LightsOut.init();
        com.eltavine.oneirgeo.world.MirrorDive.init();
        com.eltavine.oneirgeo.world.AxonCurrents.init();
        OneirgeoCommands.init();
        SelfTest.init();
    }
}
