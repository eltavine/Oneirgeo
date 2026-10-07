package com.eltavine.oneirgeo.world;

import com.eltavine.oneirgeo.block.FluorescentLightBlock;
import com.eltavine.oneirgeo.entity.Apparitions;
import com.eltavine.oneirgeo.entity.NurseEntity;
import com.eltavine.oneirgeo.entity.OneirgeoEntities;
import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

/**
 * In the backrooms the lights of a whole stretch of corridor sometimes go out together, and stay out
 * for half a minute. In the dark the night nurse does her round; stay under a light that still
 * works, or keep moving.
 */
public final class LightsOut {
    private static final int INTERVAL = 20;
    private static final float CHANCE = 0.006F;
    private static final int RADIUS = 20;
    private static final int DARK_TICKS = 700;

    private LightsOut() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % INTERVAL != 7) {
                return;
            }
            ServerLevel backrooms = server.getLevel(OneirgeoDimensions.BACKROOMS);
            if (backrooms == null) {
                return;
            }
            for (ServerPlayer player : backrooms.players()) {
                if (!player.isSpectator() && player.getRandom().nextFloat() < CHANCE) {
                    goOut(player);
                }
            }
        });
    }

    /** Puts out every light around a player; returns how many went out. */
    public static int goOut(ServerPlayer player) {
        ServerLevel level = player.level();
        List<BlockPos> lights = Mysteries.all(level, player.blockPosition(), RADIUS,
                state -> state.is(OneirgeoBlocks.FLUORESCENT_LIGHT) && state.getValue(FluorescentLightBlock.LIT));
        FluorescentLightBlock block = (FluorescentLightBlock) OneirgeoBlocks.FLUORESCENT_LIGHT;
        for (BlockPos pos : lights) {
            block.goOut(level, pos, level.getBlockState(pos), DARK_TICKS + player.getRandom().nextInt(200));
        }
        if (!lights.isEmpty()) {
            level.playSound(null, player.blockPosition(), OneirgeoSounds.LIGHTS_OUT.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
            if (level.getEntitiesOfClass(NurseEntity.class, player.getBoundingBox().inflate(48.0)).isEmpty()) {
                Apparitions.spawn(level, OneirgeoEntities.NURSE, player, 12.0, 22.0, Math.PI, Math.PI);
            }
        }
        return lights.size();
    }
}
