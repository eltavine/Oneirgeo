package com.eltavine.oneirgeo.world;

import com.eltavine.oneirgeo.network.LandmarksPayload;
import com.eltavine.oneirgeo.world.gen.OneirgeoChunkGenerator;
import com.eltavine.oneirgeo.world.gen.landmark.Landmark;
import com.eltavine.oneirgeo.world.gen.landmark.LandmarkField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Keeps every client informed of the landmarks around it, refreshing when it moves a few hundred blocks. */
public final class LandmarkSync {
    public static final int RADIUS = 3200;
    private static final int REFRESH_CELL = 384;
    private static final Map<UUID, String> LAST = new HashMap<>();

    private LandmarkSync() {
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(LandmarksPayload.TYPE, LandmarksPayload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> forget(handler.player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> forget(newPlayer));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 40 != 5) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                sync(player);
            }
        });
    }

    private static void sync(ServerPlayer player) {
        ServerLevel level = player.level();
        String key = level.dimension().identifier() + "/" + Math.floorDiv(player.getBlockX(), REFRESH_CELL) + "/" + Math.floorDiv(player.getBlockZ(), REFRESH_CELL);
        if (key.equals(LAST.get(player.getUUID()))) {
            return;
        }
        LAST.put(player.getUUID(), key);
        List<Landmark> landmarks = List.of();
        if (level.getChunkSource().getGenerator() instanceof OneirgeoChunkGenerator generator) {
            LandmarkField field = generator.landmarks();
            int x = player.getBlockX();
            int z = player.getBlockZ();
            landmarks = field.collect(generator.landmarkSeed(), x - RADIUS, z - RADIUS, x + RADIUS, z + RADIUS).stream()
                    .filter(Landmark::visible)
                    .limit(1024)
                    .toList();
        }
        ServerPlayNetworking.send(player, new LandmarksPayload(landmarks));
    }

    public static void forget(ServerPlayer player) {
        LAST.remove(player.getUUID());
    }
}
