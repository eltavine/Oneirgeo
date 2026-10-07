package com.eltavine.oneirgeo.space;

import com.eltavine.oneirgeo.registry.OneirgeoAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Reads the spatial rules around a position, on either side, without loading chunks. */
public final class SpaceQuery {
    private SpaceQuery() {
    }

    public static ChunkSpaceData at(Level level, double x, double z) {
        ChunkAccess chunk = level.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z), ChunkStatus.FULL, false);
        if (chunk == null) {
            return ChunkSpaceData.EMPTY;
        }
        ChunkSpaceData data = chunk.getAttached(OneirgeoAttachments.SPACE);
        return data == null ? ChunkSpaceData.EMPTY : data;
    }

    public static ChunkSpaceData at(Level level, BlockPos pos) {
        return at(level, pos.getX() + 0.5, pos.getZ() + 0.5);
    }

    public static boolean isTrap(Level level, Vec3 position) {
        return at(level, position.x, position.z).isTrap(position.x, position.y, position.z);
    }

    public static boolean isTrap(Level level, BlockPos pos) {
        return at(level, pos).isTrap(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static boolean isFlipped(Level level, double x, double y, double z) {
        return at(level, x, z).isFlipped(x, y, z);
    }

    /** First seam that accepts an entity at {@code position} moving by {@code motion}, or null. */
    public static @Nullable SeamVolume seamFor(Level level, Vec3 position, Vec3 motion) {
        return seamFor(level, position, motion, false);
    }

    /** Same, for someone who may remember everything: then the seams that let the awake pass do. */
    public static @Nullable SeamVolume seamFor(Level level, Vec3 position, Vec3 motion, boolean awake) {
        ChunkSpaceData data = at(level, position.x, position.z);
        for (SeamVolume seam : data.seams()) {
            if (seam.accepts(position, motion) && !(awake && seam.letsAwakePass())) {
                return seam;
            }
        }
        return null;
    }

    public static boolean hasSeam(Level level, SeamVolume seam) {
        return at(level, seam.trigger().centerX(), seam.trigger().centerZ()).seams().contains(seam);
    }
}
