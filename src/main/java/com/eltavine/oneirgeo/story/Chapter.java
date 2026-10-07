package com.eltavine.oneirgeo.story;

import com.eltavine.oneirgeo.world.OneirgeoDimensions;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;

/**
 * The six chapters of the dreamer's story, one for each dimension: what each place is made of.
 * Their entries are remembered in order, from memory fragments found in that place.
 */
public enum Chapter implements StringRepresentable {
    HOUSE("house", Level.OVERWORLD, 0xB0A49A),
    FEVER("fever", Level.NETHER, 0xC9744F),
    OBSERVATORY("observatory", Level.END, 0x9C93C8),
    REFLECTION("reflection", OneirgeoDimensions.MIRROR_SEA, 0xA9BCC4),
    POOL("pool", OneirgeoDimensions.POOLROOMS, 0x7FB8C2),
    WAITING("waiting", OneirgeoDimensions.BACKROOMS, 0xC8B76A);

    public static final int ENTRIES = 7;
    public static final Codec<Chapter> CODEC = StringRepresentable.fromEnum(Chapter::values);
    public static final StreamCodec<ByteBuf, Chapter> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], Chapter::ordinal);

    private final String id;
    private final ResourceKey<Level> dimension;
    private final int colour;

    Chapter(String id, ResourceKey<Level> dimension, int colour) {
        this.id = id;
        this.dimension = dimension;
        this.colour = colour;
    }

    /** The chapter told by a dimension; places the dream did not make remember the house. */
    public static Chapter of(ResourceKey<Level> dimension) {
        for (Chapter chapter : values()) {
            if (chapter.dimension == dimension) {
                return chapter;
            }
        }
        return HOUSE;
    }

    public String id() {
        return this.id;
    }

    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    public int colour() {
        return this.colour;
    }

    public String titleKey() {
        return "oneirgeo.story." + this.id;
    }

    public String entryKey(int entry) {
        return "oneirgeo.story." + this.id + "." + entry;
    }

    /** Lines of the recording played when the chapter is complete. */
    public String tapeKey(int line) {
        return "oneirgeo.tape." + this.id + "." + line;
    }

    public String tapeDateKey() {
        return "oneirgeo.tape." + this.id + ".date";
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }
}
