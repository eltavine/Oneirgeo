package com.eltavine.oneirgeo.world;

import com.eltavine.oneirgeo.registry.OneirgeoAttachments;
import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import com.eltavine.oneirgeo.survival.Lucidity;
import com.eltavine.oneirgeo.util.Hash;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/**
 * Falling asleep in the overworld drops you into the poolrooms; waking puts you back beside your
 * bed. You wake through a wake door, by dying, or when the dream wears your lucidity away.
 */
public final class Dreams {
    /** Ticks of sleep before the dream takes you; vanilla wakes everyone at 100. */
    private static final int FALL_ASLEEP = 60;
    private static final float WAKE_BELOW = 0.06F;
    private static final int SPREAD = 4096;

    private Dreams() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(Dreams::tick);
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Level level = player.level();
            if (player.isSleeping() && player.getSleepTimer() >= FALL_ASLEEP && level.dimension() == Level.OVERWORLD) {
                fallAsleep(player);
            } else if (level.dimension() == OneirgeoDimensions.POOLROOMS && server.getTickCount() % 20 == 0
                    && Lucidity.get(player) < WAKE_BELOW) {
                player.sendOverlayMessage(Component.translatable("oneirgeo.dream.faded"));
                wake(player);
            }
        }
    }

    private static void fallAsleep(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        ServerLevel pools = server.getLevel(OneirgeoDimensions.POOLROOMS);
        if (pools == null) {
            return;
        }
        BlockPos bed = player.getSleepingPos().orElse(player.blockPosition());
        player.setAttached(OneirgeoAttachments.RETURN_POINT, GlobalPos.of(player.level().dimension(), bed));
        player.stopSleepInBed(true, true);
        long h = Hash.of(server.overworld().getSeed(), bed.asLong(), player.getUUID().getLeastSignificantBits(), server.getTickCount());
        int x = bed.getX() + Hash.range(h, -SPREAD, SPREAD);
        int z = bed.getZ() + Hash.range(Hash.next(h, 1), -SPREAD, SPREAD);
        Passages.arrive(player, pools, x, 1, z);
        player.sendOverlayMessage(Component.translatable("oneirgeo.dream.asleep"));
    }

    public static void wake(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), OneirgeoSounds.WAKE.value(), SoundSource.PLAYERS, 0.8F, 1.0F);
        Passages.leaveDream(player);
        Lucidity.add(player, 0.3F);
    }
}
