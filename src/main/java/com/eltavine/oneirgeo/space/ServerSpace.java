package com.eltavine.oneirgeo.space;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.entity.Apparitions;
import com.eltavine.oneirgeo.network.SeamCrossPayload;
import java.util.Set;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Server side of the seams. Players predict their own crossings and report them (see
 * {@code ClientSeams}); every other entity is moved here, right after it moves.
 */
public final class ServerSpace {
    private static final double LATENCY_SLACK = 3.5;

    private ServerSpace() {
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(SeamCrossPayload.TYPE, SeamCrossPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SeamCrossPayload.TYPE, (payload, context) -> onClientCrossed(context.player(), payload.seam()));
        EntitySleepEvents.ALLOW_SETTING_SPAWN.register((player, sleepingPos) -> {
            if (SpaceQuery.isTrap(player.level(), sleepingPos)) {
                player.sendOverlayMessage(Component.translatable("oneirgeo.trap.no_spawn"));
                return false;
            }
            return true;
        });
        SelfHeal.init();
    }

    private static void onClientCrossed(ServerPlayer player, SeamVolume seam) {
        ServerLevel level = player.level();
        if (!SpaceQuery.hasSeam(level, seam) || !seam.trigger().inflate((int) Math.ceil(LATENCY_SLACK)).contains(player.getX(), player.getY(), player.getZ())) {
            Oneirgeo.LOGGER.debug("Rejected seam crossing of {} at {}", player.getPlainTextName(), player.position());
            player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
            return;
        }
        Vec3 target = seam.apply(player.position());
        player.absSnapTo(target.x, target.y, target.z, seam.rotateYaw(player.getYRot()), player.getXRot());
        player.setDeltaMovement(seam.rotateVector(player.getDeltaMovement()));
        player.connection.resetPosition();
        level.getChunkSource().move(player);
        Apparitions.followThroughSeam(player, seam);
    }

    /** Called after any non-player entity moved on the server. */
    public static void afterMove(Entity entity, Vec3 from) {
        if (entity instanceof Player || entity.isPassenger() || entity.isVehicle()) {
            return;
        }
        Vec3 position = entity.position();
        Vec3 motion = position.subtract(from);
        if (motion.lengthSqr() < 1.0E-8) {
            return;
        }
        SeamVolume seam = SpaceQuery.seamFor(entity.level(), position, motion);
        if (seam == null || entity.level().isClientSide()) {
            return;
        }
        Vec3 target = seam.apply(position);
        entity.teleportTo((ServerLevel) entity.level(), target.x, target.y, target.z, Set.<Relative>of(), seam.rotateYaw(entity.getYRot()), entity.getXRot(), false);
        entity.setDeltaMovement(seam.rotateVector(entity.getDeltaMovement()));
    }
}
