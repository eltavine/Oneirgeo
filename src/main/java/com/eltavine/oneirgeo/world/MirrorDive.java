package com.eltavine.oneirgeo.world;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import com.eltavine.oneirgeo.space.Gravity;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;

/**
 * Under the mirror sea there is another world, the same as this one, upside down. Crouch on the
 * mirror for two seconds and you sink through your own reflection, to stand on the underside of
 * the surface; crouch there to come back up.
 */
public final class MirrorDive {
    private static final int HOLD = 40;
    private static final Map<UUID, Integer> CROUCHED = new HashMap<>();

    private MirrorDive() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerLevel sea = server.getLevel(OneirgeoDimensions.MIRROR_SEA);
            if (sea == null) {
                return;
            }
            CROUCHED.keySet().removeIf(id -> {
                ServerPlayer player = server.getPlayerList().getPlayer(id);
                return player == null || player.level() != sea;
            });
            for (ServerPlayer player : sea.players()) {
                if (player.isSpectator() || !player.isShiftKeyDown() || !onMirror(player)) {
                    CROUCHED.remove(player.getUUID());
                    continue;
                }
                int held = CROUCHED.merge(player.getUUID(), 1, Integer::sum);
                if (held == 10) {
                    sea.playSound(null, player.blockPosition(), OneirgeoSounds.HEAL.value(), SoundSource.PLAYERS, 0.6F, 0.6F);
                }
                if (held >= HOLD) {
                    CROUCHED.remove(player.getUUID());
                    dive(player);
                }
            }
        });
    }

    /** Standing on the mirror: on top of it, or upside down against its underside. */
    public static boolean onMirror(ServerPlayer player) {
        BlockPos column = BlockPos.containing(player.getX(), 0.0, player.getZ());
        if (!player.level().getBlockState(column).is(OneirgeoBlocks.MIRROR_SURFACE)) {
            return false;
        }
        if (Gravity.isFlipped(player)) {
            double top = player.getY() + player.getBbHeight();
            return top > -0.3 && top <= 0.05;
        }
        return player.getY() >= 0.0 && player.getY() < 0.4;
    }

    /** Through the mirror, from its top to its underside or back; false when something stands in the way on the other side. */
    public static boolean dive(ServerPlayer player) {
        boolean below = player.getY() < 0.0;
        double y = below ? 0.1 : -player.getBbHeight() - 0.15;
        ServerLevel level = player.level();
        if (!level.noCollision(player, player.getBoundingBox().move(0.0, y - player.getY(), 0.0))) {
            player.sendOverlayMessage(Component.translatable("oneirgeo.mirror.blocked").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            return false;
        }
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), 0.0, player.getZ(), 24, 0.4, 0.1, 0.4, 0.02);
        player.teleportTo(level, player.getX(), y, player.getZ(), Set.<Relative>of(), player.getYRot(), player.getXRot(), true);
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        level.playSound(null, player.blockPosition(), OneirgeoSounds.WAKE.value(), SoundSource.PLAYERS, 0.7F, below ? 1.2F : 0.7F);
        player.sendOverlayMessage(Component.translatable(below ? "oneirgeo.mirror.surface" : "oneirgeo.mirror.dive")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        return true;
    }
}
