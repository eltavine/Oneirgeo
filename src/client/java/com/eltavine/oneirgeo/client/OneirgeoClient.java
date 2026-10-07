package com.eltavine.oneirgeo.client;

import com.eltavine.oneirgeo.client.audio.Reverb;
import com.eltavine.oneirgeo.client.config.ConfigCommand;
import com.eltavine.oneirgeo.client.config.OneirgeoConfig;
import com.eltavine.oneirgeo.client.entity.FacelessRenderer;
import com.eltavine.oneirgeo.client.entity.MimicRenderer;
import com.eltavine.oneirgeo.client.entity.StalkerRenderer;
import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.client.entity.DreamFigureRenderer;
import com.eltavine.oneirgeo.client.fx.Camcorder;
import com.eltavine.oneirgeo.client.story.JournalScreen;
import com.eltavine.oneirgeo.client.story.TapePlayback;
import com.eltavine.oneirgeo.item.DreamJournalItem;
import com.eltavine.oneirgeo.client.fx.EffectDirector;
import com.eltavine.oneirgeo.client.fx.ScreenText;
import com.eltavine.oneirgeo.client.sky.DistantGiants;
import com.eltavine.oneirgeo.client.sky.FarSilhouettes;
import com.eltavine.oneirgeo.client.space.ClientGravity;
import com.eltavine.oneirgeo.client.survival.ElevatorInput;
import com.eltavine.oneirgeo.entity.OneirgeoEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class OneirgeoClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        OneirgeoConfig.load();
        ConfigCommand.init();
        ElevatorInput.init();
        EffectDirector.init();
        ScreenText.init();
        Camcorder.init();
        TapePlayback.init();
        DreamJournalItem.opener = JournalScreen::open;
        FarSilhouettes.init();
        ClientGravity.init();
        DistantGiants.init();
        Reverb.init();
        EntityRendererRegistry.register(OneirgeoEntities.FACELESS, FacelessRenderer::new);
        EntityRendererRegistry.register(OneirgeoEntities.STALKER, StalkerRenderer::new);
        EntityRendererRegistry.register(OneirgeoEntities.MIMIC, MimicRenderer::new);
        EntityRendererRegistry.register(OneirgeoEntities.LIFEGUARD, context -> new DreamFigureRenderer<>(context, Oneirgeo.id("textures/entity/lifeguard.png")));
        EntityRendererRegistry.register(OneirgeoEntities.NURSE, context -> new DreamFigureRenderer<>(context, Oneirgeo.id("textures/entity/nurse.png")));
    }
}
