package com.eltavine.oneirgeo.survival;

import com.eltavine.oneirgeo.registry.OneirgeoAttachments;
import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import com.eltavine.oneirgeo.space.ChunkSpaceData;
import com.eltavine.oneirgeo.space.SpaceQuery;
import com.eltavine.oneirgeo.world.OneirgeoDimensions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A hidden measure of how awake the player still is, from 0 (lost) to 1 (lucid). It is never shown;
 * it only bends what happens: post effects and screen text on the client, whispers and encounters
 * on the server. Depth, darkness, dream dimensions and folded space wear it down; daylight, food,
 * sleep and lucid tea bring it back.
 */
public final class Lucidity {
    public static final float DEFAULT = 0.85F;
    private static final int STEP = 20;
    private static final Map<UUID, Integer> LAST_FOOD = new HashMap<>();

    private Lucidity() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % STEP == 10) {
                tick(server);
            }
        });
        EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayer player) {
                add(player, 0.15F);
            }
        });
    }

    public static float get(ServerPlayer player) {
        return player.getAttachedOrElse(OneirgeoAttachments.LUCIDITY, DEFAULT);
    }

    public static void set(ServerPlayer player, float value) {
        player.setAttached(OneirgeoAttachments.LUCIDITY, Mth.clamp(value, 0.0F, 1.0F));
    }

    public static void add(ServerPlayer player, float delta) {
        set(player, get(player) + delta);
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator() || !player.level().getGameRules().get(OneirgeoRules.LUCIDITY)) {
                continue;
            }
            float delta = drift(player);
            int food = player.getFoodData().getFoodLevel();
            Integer last = LAST_FOOD.put(player.getUUID(), food);
            if (last != null && food > last) {
                delta += 0.012F * (food - last);
            }
            add(player, delta);
            whisper(player);
        }
    }

    /** Change per second at the player's current position. */
    static float drift(ServerPlayer player) {
        ServerLevel level = player.level();
        BlockPos pos = player.blockPosition();
        ResourceKey<Level> dimension = level.dimension();
        int light = level.getMaxLocalRawBrightness(pos);
        float delta;
        if (dimension == Level.OVERWORLD) {
            delta = light >= 8 ? 0.0012F : -0.0016F;
        } else if (dimension == Level.NETHER) {
            delta = -0.0012F;
        } else if (dimension == Level.END) {
            delta = -0.0015F;
        } else if (dimension == OneirgeoDimensions.MIRROR_SEA) {
            delta = -0.0008F;
        } else if (dimension == OneirgeoDimensions.POOLROOMS) {
            delta = -0.0018F;
        } else if (dimension == OneirgeoDimensions.BACKROOMS) {
            delta = -0.0025F;
        } else {
            delta = 0.002F;
        }
        if (light <= 3) {
            delta -= 0.002F;
        }
        ChunkSpaceData space = SpaceQuery.at(level, pos);
        Vec3 at = player.position();
        if (space.isTrap(at.x, at.y, at.z)) {
            delta -= 0.012F;
        } else if (space.isStrict(pos.getX(), pos.getY(), pos.getZ())) {
            delta -= 0.0015F;
        }
        if (player.isSleeping()) {
            delta += 0.01F;
        }
        return delta;
    }

    /** Below one third, voices start: rarely at first, then more often. */
    private static void whisper(ServerPlayer player) {
        float lucidity = get(player);
        if (lucidity > 0.35F) {
            return;
        }
        if (player.getRandom().nextFloat() > (0.35F - lucidity) * 0.08F) {
            return;
        }
        double angle = player.getRandom().nextDouble() * Math.PI * 2;
        double x = player.getX() + Math.cos(angle) * 5.0;
        double z = player.getZ() + Math.sin(angle) * 5.0;
        player.level().playSound(null, x, player.getEyeY(), z, OneirgeoSounds.WHISPER.value(), SoundSource.AMBIENT, 0.5F, 0.8F + player.getRandom().nextFloat() * 0.3F);
    }

    public static void forget(ServerPlayer player) {
        LAST_FOOD.remove(player.getUUID());
    }
}
