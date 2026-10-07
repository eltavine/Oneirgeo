package com.eltavine.oneirgeo.survival;

import com.eltavine.oneirgeo.network.ElevatorPayload;
import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Elevator blocks: jump on one to rise to the next one straight above, sneak to sink to the next one
 * below, however far that is. Updraft wells and the stairs wound around sky pillars are the other
 * ways up; fall damage stays as it is.
 */
public final class VerticalTravel {
    private static final Map<UUID, Long> LAST_USE = new HashMap<>();

    private VerticalTravel() {
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(ElevatorPayload.TYPE, ElevatorPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ElevatorPayload.TYPE, (payload, context) -> use(context.player(), payload.up()));
    }

    private static void use(ServerPlayer player, boolean up) {
        ServerLevel level = player.level();
        long now = level.getGameTime();
        Long last = LAST_USE.get(player.getUUID());
        if (last != null && now - last < 6) {
            return;
        }
        BlockPos below = player.blockPosition().below();
        if (!level.getBlockState(below).is(OneirgeoBlocks.ELEVATOR)) {
            return;
        }
        BlockPos.MutableBlockPos cursor = below.mutable();
        int step = up ? 1 : -1;
        while (true) {
            cursor.move(0, step, 0);
            if (cursor.getY() <= level.getMinY() || cursor.getY() >= level.getMaxY() - 1) {
                return;
            }
            if (level.getBlockState(cursor).is(OneirgeoBlocks.ELEVATOR)
                    && level.getBlockState(cursor.above()).getCollisionShape(level, cursor.above()).isEmpty()
                    && level.getBlockState(cursor.above(2)).getCollisionShape(level, cursor.above(2)).isEmpty()) {
                break;
            }
        }
        LAST_USE.put(player.getUUID(), now);
        player.connection.teleport(player.getX(), cursor.getY() + 1.0, player.getZ(), player.getYRot(), player.getXRot());
        player.resetFallDistance();
        level.playSound(null, cursor.above(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.4F, up ? 1.4F : 0.7F);
    }
}
