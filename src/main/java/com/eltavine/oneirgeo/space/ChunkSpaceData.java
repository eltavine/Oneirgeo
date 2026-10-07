package com.eltavine.oneirgeo.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Spatial rules generated with a chunk. Only volumes that overlap the chunk are stored; neighbours
 * keep their own copy of shared volumes.
 *
 * @param seams          seamless teleport triggers
 * @param flipZones      regions where gravity points up
 * @param protectedZones structures that must stay identical: removed blocks and added blocks both heal
 * @param solidZones     megastructures: only generated blocks that were removed heal
 * @param trapZones      closed loops; no respawn point or supply reaches inside
 */
public record ChunkSpaceData(List<SeamVolume> seams, List<Box> flipZones, List<Box> protectedZones, List<Box> solidZones, List<Box> trapZones) {
    public static final ChunkSpaceData EMPTY = new ChunkSpaceData(List.of(), List.of(), List.of(), List.of(), List.of());

    public static final Codec<ChunkSpaceData> CODEC = RecordCodecBuilder.create(i -> i.group(
            SeamVolume.CODEC.listOf().optionalFieldOf("seams", List.of()).forGetter(ChunkSpaceData::seams),
            Box.CODEC.listOf().optionalFieldOf("flip", List.of()).forGetter(ChunkSpaceData::flipZones),
            Box.CODEC.listOf().optionalFieldOf("protected", List.of()).forGetter(ChunkSpaceData::protectedZones),
            Box.CODEC.listOf().optionalFieldOf("solid", List.of()).forGetter(ChunkSpaceData::solidZones),
            Box.CODEC.listOf().optionalFieldOf("traps", List.of()).forGetter(ChunkSpaceData::trapZones)
    ).apply(i, ChunkSpaceData::new));

    public static final StreamCodec<ByteBuf, ChunkSpaceData> STREAM_CODEC = StreamCodec.composite(
            SeamVolume.STREAM_CODEC.apply(ByteBufCodecs.list()), ChunkSpaceData::seams,
            Box.STREAM_CODEC.apply(ByteBufCodecs.list()), ChunkSpaceData::flipZones,
            Box.STREAM_CODEC.apply(ByteBufCodecs.list()), ChunkSpaceData::protectedZones,
            Box.STREAM_CODEC.apply(ByteBufCodecs.list()), ChunkSpaceData::solidZones,
            Box.STREAM_CODEC.apply(ByteBufCodecs.list()), ChunkSpaceData::trapZones,
            ChunkSpaceData::new
    );

    public boolean isEmpty() {
        return this.seams.isEmpty() && this.flipZones.isEmpty() && this.protectedZones.isEmpty() && this.solidZones.isEmpty() && this.trapZones.isEmpty();
    }

    private static boolean any(List<Box> boxes, double x, double y, double z) {
        for (Box box : boxes) {
            if (box.contains(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public boolean isStrict(int x, int y, int z) {
        return any(this.protectedZones, x + 0.5, y + 0.5, z + 0.5);
    }

    public boolean isSolidProtected(int x, int y, int z) {
        return any(this.solidZones, x + 0.5, y + 0.5, z + 0.5);
    }

    public boolean isTrap(double x, double y, double z) {
        return any(this.trapZones, x, y, z);
    }

    public boolean isFlipped(double x, double y, double z) {
        return any(this.flipZones, x, y, z);
    }

    /** Collects volumes for one chunk while it generates. */
    public static final class Builder {
        private final int minX;
        private final int minZ;
        private final List<SeamVolume> seams = new ArrayList<>();
        private final List<Box> flip = new ArrayList<>();
        private final List<Box> strict = new ArrayList<>();
        private final List<Box> solid = new ArrayList<>();
        private final List<Box> traps = new ArrayList<>();

        public Builder(int chunkMinX, int chunkMinZ) {
            this.minX = chunkMinX;
            this.minZ = chunkMinZ;
        }

        private boolean overlaps(Box box) {
            return box.intersectsColumnRange(this.minX, this.minZ, this.minX + 16, this.minZ + 16);
        }

        private void add(List<Box> list, Box box) {
            if (this.overlaps(box) && !list.contains(box)) {
                list.add(box);
            }
        }

        public void seam(SeamVolume seam) {
            if (this.overlaps(seam.trigger().inflate(2)) && !this.seams.contains(seam)) {
                this.seams.add(seam);
            }
        }

        public void flip(Box box) {
            this.add(this.flip, box);
        }

        public void protect(Box box) {
            this.add(this.strict, box);
        }

        public void protectSolid(Box box) {
            this.add(this.solid, box);
        }

        public void trap(Box box) {
            this.add(this.traps, box);
        }

        public ChunkSpaceData build() {
            if (this.seams.isEmpty() && this.flip.isEmpty() && this.strict.isEmpty() && this.solid.isEmpty() && this.traps.isEmpty()) {
                return EMPTY;
            }
            return new ChunkSpaceData(List.copyOf(this.seams), List.copyOf(this.flip), List.copyOf(this.strict), List.copyOf(this.solid), List.copyOf(this.traps));
        }
    }
}
