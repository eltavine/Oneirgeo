package com.eltavine.oneirgeo.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;

/** Original states of protected blocks that were changed, kept until the chunk heals. */
public final class HealData {
    private record Entry(long pos, BlockState state) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.LONG.fieldOf("pos").forGetter(Entry::pos),
                BlockState.CODEC.fieldOf("state").forGetter(Entry::state)
        ).apply(i, Entry::new));
    }

    public static final Codec<HealData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Entry.CODEC.listOf().fieldOf("blocks").forGetter(HealData::entries),
            Codec.LONG.fieldOf("last_change").forGetter(HealData::lastChange)
    ).apply(i, HealData::new));

    private final Long2ObjectMap<BlockState> originals = new Long2ObjectOpenHashMap<>();
    private long lastChange;

    public HealData() {
    }

    private HealData(List<Entry> entries, long lastChange) {
        for (Entry entry : entries) {
            this.originals.put(entry.pos(), entry.state());
        }
        this.lastChange = lastChange;
    }

    private List<Entry> entries() {
        List<Entry> list = new ArrayList<>(this.originals.size());
        this.originals.long2ObjectEntrySet().forEach(e -> list.add(new Entry(e.getLongKey(), e.getValue())));
        return list;
    }

    public void remember(long pos, BlockState original, long gameTime) {
        this.originals.putIfAbsent(pos, original);
        this.lastChange = gameTime;
    }

    public Long2ObjectMap<BlockState> originals() {
        return this.originals;
    }

    public long lastChange() {
        return this.lastChange;
    }

    public boolean isEmpty() {
        return this.originals.isEmpty();
    }
}
