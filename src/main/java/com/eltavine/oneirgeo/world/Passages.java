package com.eltavine.oneirgeo.world;

import com.eltavine.oneirgeo.registry.OneirgeoAttachments;
import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Moves players between dimensions through hidden doors, waking up and other non-portal routes. */
public final class Passages {
    public enum Kind {
        /** Leads from the endless rooms into the backrooms. */
        BACKROOMS,
        /** Leads back to where you last stood in a waking dimension. */
        EXIT,
        /** Ends a dream: back to the bed you fell asleep in. */
        WAKE,
        /** Looks like every other door, and never opens. */
        CLOSED
    }

    private Passages() {
    }

    public static void use(Kind kind, ServerPlayer player, BlockPos door) {
        MinecraftServer server = player.level().getServer();
        switch (kind) {
            case BACKROOMS -> {
                ServerLevel target = server.getLevel(OneirgeoDimensions.BACKROOMS);
                if (target != null) {
                    rememberReturn(player);
                    arrive(player, target, door.getX(), door.getY(), door.getZ());
                }
            }
            case EXIT -> {
                if (OneirgeoDimensions.isDream(player.level().dimension())) {
                    leaveDream(player);
                } else {
                    goHome(player);
                }
            }
            case WAKE -> Dreams.wake(player);
            case CLOSED -> {
                player.level().playSound(null, door, OneirgeoSounds.WHISPER.value(), SoundSource.BLOCKS, 0.6F, 0.6F);
                player.sendOverlayMessage(Component.translatable("oneirgeo.door.closed"));
            }
        }
    }

    /** Remembers where a player stood before entering a dream dimension. */
    public static void rememberReturn(ServerPlayer player) {
        if (!OneirgeoDimensions.isDream(player.level().dimension())) {
            player.setAttached(OneirgeoAttachments.RETURN_POINT, GlobalPos.of(player.level().dimension(), player.blockPosition()));
        }
    }

    /** Back to where the player was before the dream; travellers from the End go home instead. */
    public static void leaveDream(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        GlobalPos back = player.getAttached(OneirgeoAttachments.RETURN_POINT);
        ServerLevel target = back == null ? null : server.getLevel(back.dimension());
        if (target == null || back.dimension() == Level.END || OneirgeoDimensions.isDream(back.dimension())) {
            goHome(player);
            return;
        }
        arrive(player, target, back.pos().getX(), back.pos().getY(), back.pos().getZ());
    }

    /** The player's bed or anchor, or the world spawn; never the End or a dream. */
    public static GlobalPos home(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        ServerPlayer.RespawnConfig config = player.getRespawnConfig();
        if (config != null) {
            ResourceKey<Level> dimension = config.respawnData().dimension();
            if (server.getLevel(dimension) != null && dimension != Level.END && !OneirgeoDimensions.isDream(dimension)) {
                return GlobalPos.of(dimension, config.respawnData().pos());
            }
        }
        return GlobalPos.of(Level.OVERWORLD, server.overworld().getRespawnData().pos());
    }

    public static void goHome(ServerPlayer player) {
        GlobalPos home = home(player);
        ServerLevel level = player.level().getServer().getLevel(home.dimension());
        arrive(player, level, home.pos().getX(), home.pos().getY(), home.pos().getZ());
    }

    /** Sends a player to the nearest standing spot around a position in another (or the same) level. */
    public static void arrive(ServerPlayer player, ServerLevel target, int x, int y, int z) {
        int hint = SafeSpot.surfaceHint(target, x, y, z);
        BlockPos feet = SafeSpot.find(target, x, y, z, 4, 12);
        if (feet == null) {
            feet = SafeSpot.findOrBuild(target, x, hint, z, Blocks.SMOOTH_STONE.defaultBlockState());
        }
        travel(player, target, Vec3.atBottomCenterOf(feet));
    }

    public static void travel(ServerPlayer player, ServerLevel target, Vec3 position) {
        target.playSound(null, BlockPos.containing(position), OneirgeoSounds.PASSAGE.value(), SoundSource.PLAYERS, 0.8F, 1.0F);
        player.teleport(new TeleportTransition(target, position, Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
    }

    public static @Nullable ServerLevel level(MinecraftServer server, ResourceKey<Level> key) {
        return server.getLevel(key);
    }
}
