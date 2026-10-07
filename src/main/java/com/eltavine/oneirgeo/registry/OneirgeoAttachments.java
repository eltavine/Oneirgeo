package com.eltavine.oneirgeo.registry;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.space.ChunkSpaceData;
import com.eltavine.oneirgeo.space.HealData;
import com.eltavine.oneirgeo.story.StoryProgress;
import com.eltavine.oneirgeo.survival.Lucidity;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;

public final class OneirgeoAttachments {
    /** Seams, gravity zones, self-healing and trap volumes of a chunk; synced to every client tracking it. */
    public static final AttachmentType<ChunkSpaceData> SPACE = AttachmentRegistry.create(Oneirgeo.id("space"), builder -> builder
            .persistent(ChunkSpaceData.CODEC)
            .syncWith(ChunkSpaceData.STREAM_CODEC, OneirgeoAttachments::chunkVisible));

    /** Originals of changed blocks inside protected volumes, until the chunk heals. */
    public static final AttachmentType<HealData> HEAL = AttachmentRegistry.create(Oneirgeo.id("heal"), builder -> builder
            .persistent(HealData.CODEC)
            .initializer(HealData::new));

    /** Where a player stood in a waking dimension before slipping into a dream dimension. */
    public static final AttachmentType<GlobalPos> RETURN_POINT = AttachmentRegistry.create(Oneirgeo.id("return_point"), builder -> builder
            .persistent(GlobalPos.CODEC)
            .copyOnDeath());

    /** Ticks until the next supply delivery. */
    public static final AttachmentType<Integer> SUPPLY_TIMER = AttachmentRegistry.create(Oneirgeo.id("supply_timer"), builder -> builder
            .persistent(Codec.INT)
            .copyOnDeath());

    /** Hidden lucidity, 0 to 1; only the owner's client knows it, for its post effects. */
    public static final AttachmentType<Float> LUCIDITY = AttachmentRegistry.create(Oneirgeo.id("lucidity"), builder -> builder
            .persistent(Codec.FLOAT)
            .initializer(() -> Lucidity.DEFAULT)
            .syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.targetOnly()));

    /** What of the dreamer's story a player remembers; kept through death, known to the owner's client for the journal. */
    public static final AttachmentType<StoryProgress> STORY = AttachmentRegistry.create(Oneirgeo.id("story"), builder -> builder
            .persistent(StoryProgress.CODEC)
            .initializer(() -> StoryProgress.EMPTY)
            .copyOnDeath()
            .syncWith(StoryProgress.STREAM_CODEC, AttachmentSyncPredicate.targetOnly()));

    private OneirgeoAttachments() {
    }

    public static void init() {
    }

    /**
     * Chunk data reaches clients together with the chunk. Fabric also re-sends it while promoting a
     * freshly generated chunk, before any client can have that chunk; skip that copy.
     */
    private static boolean chunkVisible(AttachmentTarget target, ServerPlayer player) {
        if (target instanceof LevelChunk chunk && chunk.getLevel() instanceof ServerLevel level) {
            return level.getChunkSource().getChunkNow(chunk.getPos().x(), chunk.getPos().z()) == chunk;
        }
        return true;
    }
}
