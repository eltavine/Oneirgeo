package com.eltavine.oneirgeo.world;

import com.eltavine.oneirgeo.block.ClockBlock;
import com.eltavine.oneirgeo.block.TelephoneBlock;
import com.eltavine.oneirgeo.block.TelevisionBlock;
import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import com.eltavine.oneirgeo.story.Story;
import com.eltavine.oneirgeo.survival.Lucidity;
import com.eltavine.oneirgeo.world.gen.OneirgeoChunkGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jspecify.annotations.Nullable;

/**
 * Small things that happen when you are alone: a telephone rings, a television wakes up to snow, the
 * stopped clocks tick once, someone knocks three times, the camcorder plays an old tape for a moment.
 * Rare, never a fright, nothing that flickers.
 */
public final class Mysteries {
    public enum Kind {
        RING,
        TELEVISION,
        CLOCK,
        KNOCK,
        GLITCH
    }

    private static final int INTERVAL = 20;
    private static final float CHANCE = 0.0055F;
    private static final int COOLDOWN = 1200;
    private static final Map<UUID, Long> LAST = new HashMap<>();
    private static final List<Knock> KNOCKS = new ArrayList<>();

    private record Knock(ServerLevel level, BlockPos pos, long at) {
    }

    private Mysteries() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(Mysteries::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LAST.remove(handler.getPlayer().getUUID()));
    }

    private static void tick(MinecraftServer server) {
        long now = server.getTickCount();
        KNOCKS.removeIf(knock -> {
            if (knock.at() > now) {
                return false;
            }
            knock.level().playSound(null, knock.pos(), OneirgeoSounds.KNOCK.value(), SoundSource.BLOCKS, 0.9F, 1.0F);
            return true;
        });
        if (now % INTERVAL != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator() || !(player.level().getChunkSource().getGenerator() instanceof OneirgeoChunkGenerator)) {
                continue;
            }
            Long last = LAST.get(player.getUUID());
            if ((last != null && now - last < COOLDOWN) || player.getRandom().nextFloat() > CHANCE) {
                continue;
            }
            Kind kind = Kind.values()[player.getRandom().nextInt(Kind.values().length)];
            if (happen(player, kind)) {
                LAST.put(player.getUUID(), now);
            }
        }
    }

    /** Makes one of them happen around a player; false when there is nothing nearby for it. */
    public static boolean happen(ServerPlayer player, Kind kind) {
        ServerLevel level = player.level();
        BlockPos at = player.blockPosition();
        switch (kind) {
            case RING -> {
                BlockPos phone = nearest(level, at, 24, state -> state.is(OneirgeoBlocks.TELEPHONE));
                if (phone == null) {
                    return false;
                }
                ((TelephoneBlock) OneirgeoBlocks.TELEPHONE).ring(level, phone, level.getBlockState(phone));
                return true;
            }
            case TELEVISION -> {
                BlockPos screen = nearest(level, at, 24, state -> state.is(OneirgeoBlocks.TELEVISION) && !state.getValue(TelevisionBlock.PLAYING));
                if (screen == null) {
                    return false;
                }
                ((TelevisionBlock) OneirgeoBlocks.TELEVISION).switchOn(level, screen, level.getBlockState(screen), 240);
                return true;
            }
            case CLOCK -> {
                List<BlockPos> clocks = all(level, at, 24, state -> state.is(OneirgeoBlocks.STOPPED_CLOCK));
                clocks.forEach(clock -> ClockBlock.tickOnce(level, clock));
                return !clocks.isEmpty();
            }
            case KNOCK -> {
                BlockPos door = nearest(level, at, 12, state -> state.getBlock() instanceof DoorBlock);
                if (door == null) {
                    return false;
                }
                long now = level.getServer().getTickCount();
                for (int i = 0; i < 3; i++) {
                    KNOCKS.add(new Knock(level, door, now + i * 7L));
                }
                return true;
            }
            case GLITCH -> {
                if (Lucidity.get(player) > 0.75F) {
                    return false;
                }
                Story.play(player, "glitch");
                return true;
            }
        }
        return false;
    }

    /** The closest matching block among the loaded chunks within {@code radius}, or null. */
    public static @Nullable BlockPos nearest(ServerLevel level, BlockPos centre, int radius, Predicate<BlockState> match) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : all(level, centre, radius, match)) {
            double d = pos.distSqr(centre);
            if (d < bestDistance) {
                bestDistance = d;
                best = pos;
            }
        }
        return best;
    }

    /** Every matching block among the loaded chunks within {@code radius}; sections without one are skipped by their palette. */
    public static List<BlockPos> all(ServerLevel level, BlockPos centre, int radius, Predicate<BlockState> match) {
        List<BlockPos> found = new ArrayList<>();
        int minY = Math.max(level.getMinY(), centre.getY() - radius);
        int maxY = Math.min(level.getMaxY(), centre.getY() + radius);
        for (int cx = SectionPos.blockToSectionCoord(centre.getX() - radius); cx <= SectionPos.blockToSectionCoord(centre.getX() + radius); cx++) {
            for (int cz = SectionPos.blockToSectionCoord(centre.getZ() - radius); cz <= SectionPos.blockToSectionCoord(centre.getZ() + radius); cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) {
                    continue;
                }
                for (int sy = SectionPos.blockToSectionCoord(minY); sy <= SectionPos.blockToSectionCoord(maxY); sy++) {
                    LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sy));
                    if (section.hasOnlyAir() || !section.maybeHas(match)) {
                        continue;
                    }
                    for (int y = 0; y < 16; y++) {
                        int wy = (sy << 4) + y;
                        if (wy < minY || wy > maxY) {
                            continue;
                        }
                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                if (match.test(section.getBlockState(x, y, z))) {
                                    BlockPos pos = new BlockPos((cx << 4) + x, wy, (cz << 4) + z);
                                    if (pos.distSqr(centre) <= (double) radius * radius) {
                                        found.add(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return found;
    }
}
