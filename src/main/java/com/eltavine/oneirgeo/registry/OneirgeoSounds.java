package com.eltavine.oneirgeo.registry;

import com.eltavine.oneirgeo.Oneirgeo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/** Sound events; their files are vanilla recordings slowed, pitched and layered in sounds.json. */
public final class OneirgeoSounds {
    private static final List<Holder.Reference<SoundEvent>> ALL = new ArrayList<>();

    public static final Holder.Reference<SoundEvent> AMBIENT_DUSK = register("ambient.dusk");
    public static final Holder.Reference<SoundEvent> AMBIENT_HIGH_WIND = register("ambient.high_wind");
    public static final Holder.Reference<SoundEvent> AMBIENT_ROOMS = register("ambient.rooms");
    public static final Holder.Reference<SoundEvent> AMBIENT_ROOMS_MOOD = register("ambient.rooms.mood");
    public static final Holder.Reference<SoundEvent> AMBIENT_FURNACE = register("ambient.furnace");
    public static final Holder.Reference<SoundEvent> AMBIENT_FURNACE_MOOD = register("ambient.furnace.mood");
    public static final Holder.Reference<SoundEvent> AMBIENT_HUM = register("ambient.hum");
    public static final Holder.Reference<SoundEvent> AMBIENT_VOID = register("ambient.void");
    public static final Holder.Reference<SoundEvent> AMBIENT_SEA = register("ambient.sea");
    public static final Holder.Reference<SoundEvent> AMBIENT_POOL = register("ambient.pool");
    public static final Holder.Reference<SoundEvent> AMBIENT_POOL_DRIP = register("ambient.pool.drip");
    public static final Holder.Reference<SoundEvent> AMBIENT_CLOSED = register("ambient.closed");
    public static final Holder.Reference<SoundEvent> AMBIENT_FOG = register("ambient.fog");
    public static final Holder.Reference<SoundEvent> AMBIENT_PULSE = register("ambient.pulse");
    public static final Holder.Reference<SoundEvent> AMBIENT_CURRENT = register("ambient.current");
    public static final Holder.Reference<SoundEvent> MUSIC_DREAM = register("music.dream");
    public static final Holder.Reference<SoundEvent> MUSIC_DEEP = register("music.deep");
    public static final Holder.Reference<SoundEvent> MUSIC_FURNACE = register("music.furnace");
    public static final Holder.Reference<SoundEvent> MUSIC_VOID = register("music.void");
    public static final Holder.Reference<SoundEvent> WHISPER = register("whisper");
    public static final Holder.Reference<SoundEvent> SUPPLY = register("supply");
    public static final Holder.Reference<SoundEvent> PASSAGE = register("passage");
    public static final Holder.Reference<SoundEvent> WAKE = register("wake");
    public static final Holder.Reference<SoundEvent> HEAL = register("heal");
    public static final Holder.Reference<SoundEvent> ENTITY_FIGURE = register("entity.figure");
    public static final Holder.Reference<SoundEvent> ENTITY_STALKER = register("entity.stalker");
    public static final Holder.Reference<SoundEvent> TELEPHONE_RING = register("block.telephone.ring");
    public static final Holder.Reference<SoundEvent> TELEPHONE_LINE = register("block.telephone.line");
    public static final Holder.Reference<SoundEvent> TV_ON = register("block.television.on");
    public static final Holder.Reference<SoundEvent> TAPE_STATIC = register("tape.static");
    public static final Holder.Reference<SoundEvent> CLOCK_TICK = register("block.clock.tick");
    public static final Holder.Reference<SoundEvent> MONITOR_BEEP = register("block.monitor.beep");
    public static final Holder.Reference<SoundEvent> KNOCK = register("knock");
    public static final Holder.Reference<SoundEvent> LIGHTS_OUT = register("lights_out");

    private OneirgeoSounds() {
    }

    private static Holder.Reference<SoundEvent> register(String name) {
        Identifier id = Oneirgeo.id(name);
        Holder.Reference<SoundEvent> holder = Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
        ALL.add(holder);
        return holder;
    }

    public static List<Holder.Reference<SoundEvent>> all() {
        return Collections.unmodifiableList(ALL);
    }

    public static void init() {
    }
}
