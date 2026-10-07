package com.eltavine.oneirgeo.world;

import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import com.eltavine.oneirgeo.world.gen.LayoutSampler;
import com.eltavine.oneirgeo.world.gen.OneirgeoChunkGenerator;
import com.eltavine.oneirgeo.world.gen.scene.neural.NeuralWebScene;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The hollow axons of the overworld still carry their signal: inside one, a current takes you along
 * it from the neuron it leaves towards the one it reaches, faster than you could walk, with sparks
 * at your heels. Walking against it is slow work.
 */
public final class AxonCurrents {
    private static final double PUSH = 0.075;
    private static final double MAX = 0.8;
    private static final Map<UUID, double[]> FLOW = new HashMap<>();

    private AxonCurrents() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(AxonCurrents::tick);
    }

    private static void tick(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        if (!(overworld.getChunkSource().getGenerator() instanceof OneirgeoChunkGenerator)) {
            return;
        }
        int now = server.getTickCount();
        FLOW.keySet().removeIf(id -> {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            return player == null || player.level() != overworld;
        });
        for (ServerPlayer player : overworld.players()) {
            if (player.isSpectator() || player.getAbilities().flying) {
                FLOW.remove(player.getUUID());
                continue;
            }
            double[] flow;
            if ((now + player.getId()) % 4 == 0) {
                flow = flowAt(overworld, player.getX(), player.getY() + 0.9, player.getZ());
                if (flow == null) {
                    FLOW.remove(player.getUUID());
                } else {
                    FLOW.put(player.getUUID(), flow);
                }
            } else {
                flow = FLOW.get(player.getUUID());
            }
            if (flow != null) {
                carry(overworld, player, flow, now);
            }
        }
    }

    /** The current at a point of the overworld, or null outside the bore of every axon. */
    public static double @Nullable [] flowAt(ServerLevel overworld, double x, double y, double z) {
        if (!(overworld.getChunkSource().getGenerator() instanceof OneirgeoChunkGenerator generator)) {
            return null;
        }
        LayoutSampler sampler = generator.sampler(overworld);
        int layer = sampler.layerIndex((int) Math.floor(y));
        if (layer < 0) {
            return null;
        }
        return NeuralWebScene.axonFlow(sampler.layerSeed(layer), x, y, z);
    }

    /** One tick of being carried; also used by the self-test. */
    public static void carry(ServerLevel level, ServerPlayer player, double[] flow, int now) {
        Vec3 motion = player.getDeltaMovement();
        double along = motion.x * flow[0] + motion.y * flow[1] + motion.z * flow[2];
        if (along < MAX) {
            double lift = flow[1] > 0.0 ? 0.03 : 0.0;
            player.setDeltaMovement(motion.add(flow[0] * PUSH, flow[1] * PUSH + lift, flow[2] * PUSH));
            player.syncVelocity = true;
        }
        player.resetFallDistance();
        if (now % 8 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 0.3, player.getZ(), 4, 0.3, 0.2, 0.3, 0.05);
        }
        if (now % 40 == 0) {
            level.playSound(null, player.blockPosition(), OneirgeoSounds.AMBIENT_CURRENT.value(), SoundSource.AMBIENT, 0.6F, 1.0F);
        }
    }
}
