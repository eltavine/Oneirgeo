package com.eltavine.oneirgeo.space;

import com.eltavine.oneirgeo.registry.OneirgeoAttachments;
import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

/**
 * Protected structures put themselves back once nobody is looking: removed blocks return, and in
 * strict zones anything added is pushed out as an item (container contents included).
 */
public final class SelfHeal {
    private static final int CHECK_INTERVAL = 40;
    private static final long QUIET_TICKS = 400;
    private static final double NEAR = 28.0;
    private static final double SIGHT = 112.0;
    private static final double SIGHT_COS = 0.42;
    private static final Map<ResourceKey<Level>, LongSet> PENDING = new HashMap<>();
    private static boolean healing;

    private SelfHeal() {
    }

    static void init() {
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
            if (chunk.hasAttached(OneirgeoAttachments.HEAL)) {
                pending(level).add(chunk.getPos().pack());
            }
        });
        ServerTickEvents.END_LEVEL_TICK.register(level -> {
            if (level.getGameTime() % CHECK_INTERVAL == 0) {
                tick(level);
            }
        });
    }

    private static LongSet pending(ServerLevel level) {
        return PENDING.computeIfAbsent(level.dimension(), k -> new LongOpenHashSet());
    }

    /** Called from {@code LevelChunkMixin} after a block of a full chunk changed. */
    public static void blockChanged(LevelChunk chunk, BlockPos pos, BlockState before, BlockState after) {
        if (healing || !(chunk.getLevel() instanceof ServerLevel level) || !level.getServer().isSameThread()) {
            return;
        }
        ChunkSpaceData space = chunk.getAttached(OneirgeoAttachments.SPACE);
        if (space == null) {
            return;
        }
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean strict = space.isStrict(x, y, z);
        if (!strict && !(space.isSolidProtected(x, y, z) && !before.isAir())) {
            return;
        }
        HealData data = chunk.getAttachedOrCreate(OneirgeoAttachments.HEAL);
        data.remember(pos.asLong(), before, level.getGameTime());
        chunk.markUnsaved();
        pending(level).add(chunk.getPos().pack());
    }

    private static void tick(ServerLevel level) {
        LongSet set = PENDING.get(level.dimension());
        if (set == null || set.isEmpty()) {
            return;
        }
        LongIterator iterator = set.iterator();
        while (iterator.hasNext()) {
            long key = iterator.nextLong();
            LevelChunk chunk = level.getChunkSource().getChunkNow(ChunkPos.getX(key), ChunkPos.getZ(key));
            if (chunk == null) {
                iterator.remove();
                continue;
            }
            HealData data = chunk.getAttached(OneirgeoAttachments.HEAL);
            if (data == null || data.isEmpty()) {
                chunk.removeAttached(OneirgeoAttachments.HEAL);
                iterator.remove();
                continue;
            }
            if (level.getGameTime() - data.lastChange() < QUIET_TICKS || observed(level, chunk.getPos(), data)) {
                continue;
            }
            heal(level, data);
            chunk.removeAttached(OneirgeoAttachments.HEAL);
            chunk.markUnsaved();
            iterator.remove();
        }
    }

    private static boolean observed(ServerLevel level, ChunkPos chunkPos, HealData data) {
        long any = data.originals().keySet().iterator().nextLong();
        Vec3 centre = new Vec3(chunkPos.getMiddleBlockX() + 0.5, BlockPos.getY(any) + 0.5, chunkPos.getMiddleBlockZ() + 0.5);
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            Vec3 eye = player.getEyePosition();
            Vec3 toward = centre.subtract(eye);
            double distance = toward.length();
            if (distance < NEAR) {
                return true;
            }
            if (distance < SIGHT && player.getViewVector(1.0F).dot(toward.scale(1.0 / distance)) > SIGHT_COS) {
                return true;
            }
        }
        return false;
    }

    private static void heal(ServerLevel level, HealData data) {
        healing = true;
        try {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            BlockPos sound = null;
            for (Long2ObjectMap.Entry<BlockState> entry : data.originals().long2ObjectEntrySet()) {
                pos.set(entry.getLongKey());
                BlockState original = entry.getValue();
                BlockState current = level.getBlockState(pos);
                if (current == original) {
                    continue;
                }
                if (!current.isAir() && current.getFluidState().isEmpty()) {
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof Container container) {
                        Containers.dropContents(level, pos, container);
                    }
                    if (current.getBlock().asItem() != Items.AIR) {
                        Block.popResource(level, pos, new ItemStack(current.getBlock().asItem()));
                    }
                }
                level.setBlock(pos, original, Block.UPDATE_ALL);
                sound = pos.immutable();
            }
            if (sound != null) {
                level.playSound(null, sound, OneirgeoSounds.HEAL.value(), SoundSource.BLOCKS, 0.3F, 1.0F);
            }
        } finally {
            healing = false;
        }
    }
}
