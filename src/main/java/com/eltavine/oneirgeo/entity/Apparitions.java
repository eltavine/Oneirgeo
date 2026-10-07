package com.eltavine.oneirgeo.entity;

import com.eltavine.oneirgeo.space.SeamVolume;
import com.eltavine.oneirgeo.world.OneirgeoDimensions;
import com.eltavine.oneirgeo.world.SafeSpot;
import com.eltavine.oneirgeo.world.gen.OneirgeoChunkGenerator;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Who you meet, and how rarely: a faceless figure in the fog, the stalker that comes for one player
 * at a time, and faded, silent animals that disappear once nobody is near. All of them only in the
 * dimensions this mod generates.
 */
public final class Apparitions {
    private static final int INTERVAL = 200;
    private static final String FADED = "oneirgeo_faded";
    private static final List<EntityType<? extends Animal>> ANIMALS = List.of(EntityTypes.COW, EntityTypes.PIG, EntityTypes.CHICKEN);

    private Apparitions() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % INTERVAL == 0) {
                tick(server);
            }
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Animal && dreamed(level)) {
                entity.setSilent(true);
            }
        });
    }

    private static boolean dreamed(ServerLevel level) {
        return level.getChunkSource().getGenerator() instanceof OneirgeoChunkGenerator;
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerLevel level = player.level();
            if (player.isSpectator() || !dreamed(level)) {
                continue;
            }
            RandomSource random = level.getRandom();
            boolean hostile = level.getDifficulty() != Difficulty.PEACEFUL;
            if (hostile && random.nextFloat() < 0.035F && none(level, StalkerEntity.class, player, 160.0)) {
                StalkerEntity stalker = spawn(level, OneirgeoEntities.STALKER, player, 30.0, 40.0, Math.PI, 0.6);
                if (stalker != null) {
                    stalker.follow(player);
                }
            }
            if (random.nextFloat() < 0.07F && none(level, FacelessEntity.class, player, 96.0)) {
                spawn(level, OneirgeoEntities.FACELESS, player, 24.0, 40.0, 0.0, 1.2);
            }
            if (level.dimension() == OneirgeoDimensions.POOLROOMS && random.nextFloat() < 0.5F) {
                LifeguardEntity.post(level, player);
            }
            boolean grazing = level.dimension() == Level.OVERWORLD || level.dimension() == OneirgeoDimensions.MIRROR_SEA
                    || level.dimension() == OneirgeoDimensions.POOLROOMS;
            if (grazing && random.nextFloat() < 0.08F
                    && level.getEntitiesOfClass(Animal.class, player.getBoundingBox().inflate(64.0), a -> a.entityTags().contains(FADED)).size() < 4) {
                Animal animal = spawn(level, ANIMALS.get(random.nextInt(ANIMALS.size())), player, 16.0, 32.0, 0.0, Math.PI);
                if (animal != null) {
                    animal.addTag(FADED);
                }
            }
            for (Animal faded : level.getEntitiesOfClass(Animal.class, player.getBoundingBox().inflate(160.0), a -> a.entityTags().contains(FADED))) {
                if (level.getNearestPlayer(faded, 96.0) == null) {
                    faded.discard();
                }
            }
        }
    }

    private static boolean none(ServerLevel level, Class<? extends Entity> type, ServerPlayer player, double range) {
        return level.getEntitiesOfClass(type, player.getBoundingBox().inflate(range)).isEmpty();
    }

    /**
     * Spawns somewhere a mob can stand, {@code min} to {@code max} blocks from the player, within
     * {@code spread} radians of the direction {@code turn} radians from where the player looks.
     */
    public static <T extends Mob> @Nullable T spawn(ServerLevel level, EntityType<T> type, ServerPlayer player, double min, double max,
                                                    double turn, double spread) {
        RandomSource random = level.getRandom();
        double yaw = Math.toRadians(player.getYRot()) + turn + (random.nextDouble() * 2.0 - 1.0) * spread;
        double distance = min + random.nextDouble() * (max - min);
        int x = Mth.floor(player.getX() - Math.sin(yaw) * distance);
        int z = Mth.floor(player.getZ() + Math.cos(yaw) * distance);
        BlockPos feet = SafeSpot.find(level, x, player.getBlockY(), z, 4, 16);
        if (feet == null) {
            return null;
        }
        T mob = type.create(level, EntitySpawnReason.EVENT);
        if (mob == null) {
            return null;
        }
        mob.snapTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(mob);
        return mob;
    }

    /** A stalker keeps its place behind the player it follows, even when the player steps through a seam. */
    public static void followThroughSeam(ServerPlayer player, SeamVolume seam) {
        ServerLevel level = player.level();
        for (StalkerEntity stalker : level.getEntitiesOfClass(StalkerEntity.class, player.getBoundingBox().inflate(128.0), s -> s.follows(player))) {
            Vec3 target = seam.apply(stalker.position());
            stalker.teleportTo(target.x, target.y, target.z);
            stalker.setYRot(seam.rotateYaw(stalker.getYRot()));
            stalker.getNavigation().stop();
        }
    }
}
