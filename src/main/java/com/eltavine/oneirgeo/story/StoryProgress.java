package com.eltavine.oneirgeo.story;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * How much of the story a player remembers: entries per chapter, a few one-time flags, and the
 * place written on the back of the star map once that memory has come back.
 *
 * @param remembered entries remembered of each {@link Chapter}, in chapter order
 * @param flags      {@link #JOURNAL}, {@link #FINAL} and so on
 * @param secretX    x of the time capsule, meaningful once {@link #SECRET} is set
 * @param secretZ    z of the time capsule
 */
public record StoryProgress(List<Integer> remembered, int flags, int secretX, int secretZ) {
    /** The player has been given the dream journal. */
    public static final int JOURNAL = 1;
    /** The player has seen the room where the clocks stopped. */
    public static final int FINAL = 1 << 1;
    /** The player knows where the time capsule is buried. */
    public static final int SECRET = 1 << 2;
    /** The player has opened the time capsule. */
    public static final int CAPSULE = 1 << 3;

    public static final StoryProgress EMPTY = new StoryProgress(List.of(0, 0, 0, 0, 0, 0), 0, 0, 0);
    public static final Codec<StoryProgress> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.listOf().fieldOf("remembered").forGetter(StoryProgress::remembered),
            Codec.INT.optionalFieldOf("flags", 0).forGetter(StoryProgress::flags),
            Codec.INT.optionalFieldOf("secret_x", 0).forGetter(StoryProgress::secretX),
            Codec.INT.optionalFieldOf("secret_z", 0).forGetter(StoryProgress::secretZ)
    ).apply(i, StoryProgress::new));
    public static final StreamCodec<ByteBuf, StoryProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), StoryProgress::remembered,
            ByteBufCodecs.VAR_INT, StoryProgress::flags,
            ByteBufCodecs.VAR_INT, StoryProgress::secretX,
            ByteBufCodecs.VAR_INT, StoryProgress::secretZ,
            StoryProgress::new);

    public int remembered(Chapter chapter) {
        return chapter.ordinal() < this.remembered.size() ? this.remembered.get(chapter.ordinal()) : 0;
    }

    public boolean complete(Chapter chapter) {
        return this.remembered(chapter) >= Chapter.ENTRIES;
    }

    public boolean allComplete() {
        for (Chapter chapter : Chapter.values()) {
            if (!this.complete(chapter)) {
                return false;
            }
        }
        return true;
    }

    public int total() {
        int sum = 0;
        for (Chapter chapter : Chapter.values()) {
            sum += this.remembered(chapter);
        }
        return sum;
    }

    public boolean has(int flag) {
        return (this.flags & flag) != 0;
    }

    public StoryProgress remember(Chapter chapter) {
        List<Integer> next = new ArrayList<>(this.remembered);
        while (next.size() < Chapter.values().length) {
            next.add(0);
        }
        next.set(chapter.ordinal(), Math.min(Chapter.ENTRIES, next.get(chapter.ordinal()) + 1));
        return new StoryProgress(List.copyOf(next), this.flags, this.secretX, this.secretZ);
    }

    public StoryProgress with(int flag) {
        return new StoryProgress(this.remembered, this.flags | flag, this.secretX, this.secretZ);
    }

    public StoryProgress withSecret(int x, int z) {
        return new StoryProgress(this.remembered, this.flags | SECRET, x, z);
    }
}
