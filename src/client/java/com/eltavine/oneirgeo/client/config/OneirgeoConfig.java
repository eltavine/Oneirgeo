package com.eltavine.oneirgeo.client.config;

import com.eltavine.oneirgeo.Oneirgeo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Client settings, stored in {@code config/oneirgeo-client.json}. Safe mode removes everything that
 * flickers or jitters (grain, JPEG blocks, chromatic jitter, lens wobble, flashing text) for
 * photosensitive players, and slows down every remaining transition.
 */
public final class OneirgeoConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("oneirgeo-client.json");
    private static OneirgeoConfig instance = new OneirgeoConfig();

    public boolean effects = true;
    public boolean safeMode = false;
    public float intensity = 1.0F;
    public float bloom = 1.0F;
    public float vignette = 1.0F;
    public float exposure = 1.0F;
    public float chromatic = 1.0F;
    public float grain = 1.0F;
    public float pixelate = 1.0F;
    public float jpeg = 1.0F;
    public float lens = 1.0F;
    public boolean screenText = true;
    public boolean farSilhouettes = true;
    public boolean wrongSky = true;
    public boolean reverb = true;
    /** The handheld camcorder look: tape filter, shaking picture and viewfinder overlay. */
    public boolean camcorder = true;
    public float shake = 1.0F;

    public static OneirgeoConfig get() {
        return instance;
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                OneirgeoConfig loaded = GSON.fromJson(reader, OneirgeoConfig.class);
                if (loaded != null) {
                    instance = loaded.sanitized();
                }
            } catch (IOException | RuntimeException e) {
                Oneirgeo.LOGGER.warn("Could not read {}, using defaults", PATH, e);
                instance = new OneirgeoConfig();
            }
        }
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            Oneirgeo.LOGGER.warn("Could not write {}", PATH, e);
        }
    }

    private static float clamp(float value) {
        return Float.isFinite(value) ? Math.max(0.0F, Math.min(2.0F, value)) : 1.0F;
    }

    private OneirgeoConfig sanitized() {
        this.intensity = clamp(this.intensity);
        this.bloom = clamp(this.bloom);
        this.vignette = clamp(this.vignette);
        this.exposure = clamp(this.exposure);
        this.chromatic = clamp(this.chromatic);
        this.grain = clamp(this.grain);
        this.pixelate = clamp(this.pixelate);
        this.jpeg = clamp(this.jpeg);
        this.lens = clamp(this.lens);
        this.shake = clamp(this.shake);
        return this;
    }

    /** Scales an effect by the master intensity, and drops flickering effects entirely in safe mode. */
    public float scaled(float perEffect, boolean flickers) {
        if (!this.effects || (flickers && this.safeMode)) {
            return 0.0F;
        }
        return perEffect * this.intensity;
    }
}
