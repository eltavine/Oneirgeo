package com.eltavine.oneirgeo.entity;

import com.eltavine.oneirgeo.world.SafeSpot;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * The lifeguard of the September pool, on his tall chair, never blinking. Stay under too long and
 * he blows his whistle, and you are lying on the tiles at his feet, coughing.
 */
public class LifeguardEntity extends PathfinderMob {
    private static final double WATCH = 40.0;
    private static final int CHECK = 10;
    private static final int UNDER_TICKS = 80;
    private final Map<UUID, Integer> under = new HashMap<>();

    public LifeguardEntity(EntityType<? extends LifeguardEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.FOLLOW_RANGE, WATCH);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, (float) WATCH, 1.0F));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount % CHECK != 0) {
            return;
        }
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(WATCH))) {
            if (player.isSpectator() || player.isCreative() || !player.isUnderWater()) {
                this.under.remove(player.getUUID());
                continue;
            }
            int ticks = this.under.merge(player.getUUID(), CHECK, Integer::sum);
            if (ticks >= UNDER_TICKS) {
                this.under.remove(player.getUUID());
                this.rescue(level, player);
            }
        }
        this.under.keySet().removeIf(id -> level.getPlayerByUUID(id) == null);
    }

    /** Whistle, and the swimmer is out of the water beside the chair. True when there was somewhere to put them. */
    public boolean rescue(ServerLevel level, ServerPlayer player) {
        level.playSound(null, this.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.NEUTRAL, 1.2F, 2.0F);
        BlockPos spot = this.dryStanding(level);
        if (spot == null) {
            return false;
        }
        player.teleportTo(level, spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, java.util.Set.<Relative>of(), player.getYRot(), player.getXRot(), true);
        player.setAirSupply(player.getMaxAirSupply());
        player.resetFallDistance();
        level.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getY() + 0.5, player.getZ(), 20, 0.4, 0.4, 0.4, 0.1);
        player.sendOverlayMessage(Component.translatable("oneirgeo.lifeguard.rescue").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        return true;
    }

    private @Nullable BlockPos dryStanding(ServerLevel level) {
        BlockPos base = this.blockPosition();
        for (int r = 1; r <= 6; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    for (int dy = -6; dy <= 1; dy++) {
                        BlockPos feet = base.offset(dx, dy, dz);
                        if (level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty()
                                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                                && !level.getBlockState(feet.below()).getCollisionShape(level, feet.below()).isEmpty()
                                && level.getFluidState(feet.below()).isEmpty()) {
                            return feet;
                        }
                    }
                }
            }
        }
        return SafeSpot.find(level, base.getX(), base.getY(), base.getZ(), 4, 8);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 1.0, this.getZ(), 16, 0.3, 0.6, 0.3, 0.1);
        this.discard();
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return true;
    }

    /**
     * Seats a lifeguard at the nearest lifeguard chair (a chair on a white pillar) within 32 blocks
     * of the player, unless one already watches this pool. Returns the lifeguard, or null.
     */
    public static @Nullable LifeguardEntity post(ServerLevel level, ServerPlayer player) {
        if (!level.getEntitiesOfClass(LifeguardEntity.class, player.getBoundingBox().inflate(48.0)).isEmpty()) {
            return null;
        }
        BlockPos chair = null;
        double best = Double.MAX_VALUE;
        for (BlockPos seat : com.eltavine.oneirgeo.world.Mysteries.all(level, player.blockPosition(), 32,
                state -> state.is(com.eltavine.oneirgeo.registry.OneirgeoBlocks.WAITING_CHAIR))) {
            double d = seat.distSqr(player.blockPosition());
            if (d < best && level.getBlockState(seat.below()).is(net.minecraft.world.level.block.Blocks.QUARTZ_PILLAR)) {
                best = d;
                chair = seat;
            }
        }
        if (chair == null) {
            return null;
        }
        BlockPos feet = chair.below(3).north();
        LifeguardEntity guard = OneirgeoEntities.LIFEGUARD.create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        if (guard == null || !level.getBlockState(feet).isAir()) {
            return null;
        }
        guard.snapTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, -90.0F, 0.0F);
        level.addFreshEntity(guard);
        return guard;
    }
}
