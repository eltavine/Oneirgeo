package com.eltavine.oneirgeo.entity;

import com.eltavine.oneirgeo.world.OneirgeoDimensions;
import com.eltavine.oneirgeo.world.Passages;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.Level;

/**
 * The night nurse of the waiting rooms, doing her rounds in the corridors. In the light she passes
 * you by. Stand in the dark and she comes for you, takes your hand, and puts you back to bed.
 */
public class NurseEntity extends PathfinderMob {
    private static final double SEEK = 20.0;
    private static final int DARK = 4;

    public NurseEntity(EntityType<? extends NurseEntity> type, Level level) {
        super(type, level);
        this.setSilent(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, SEEK);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount % 5 != 0) {
            return;
        }
        ServerPlayer target = null;
        double best = SEEK * SEEK;
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(SEEK))) {
            if (player.isSpectator() || player.isCreative() || level.getMaxLocalRawBrightness(player.blockPosition()) > DARK) {
                continue;
            }
            double d = player.distanceToSqr(this);
            if (d < best) {
                best = d;
                target = player;
            }
        }
        if (target == null) {
            return;
        }
        if (best < 2.6) {
            this.putBackToBed(level, target);
            return;
        }
        this.getNavigation().moveTo(target, 1.0);
    }

    /** Back to where the player came from (or home), and she is gone. */
    public void putBackToBed(ServerLevel level, ServerPlayer player) {
        player.sendOverlayMessage(Component.translatable("oneirgeo.nurse.bed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        if (OneirgeoDimensions.isDream(level.dimension())) {
            Passages.leaveDream(player);
        } else {
            Passages.goHome(player);
        }
        level.sendParticles(ParticleTypes.WHITE_ASH, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.3, 0.8, 0.3, 0.0);
        this.discard();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        level.sendParticles(ParticleTypes.WHITE_ASH, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.3, 0.8, 0.3, 0.0);
        this.discard();
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return true;
    }
}
