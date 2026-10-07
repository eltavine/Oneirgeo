package com.eltavine.oneirgeo.entity;

import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A pale figure with no face, standing where the fog thins, always turned towards you. Come close,
 * stare too long or touch it, and it is not there any more.
 */
public class FacelessEntity extends PathfinderMob {
    private static final double NEAR = 9.0;
    private static final double STARE_COS = 0.985;
    private static final int STARE_TICKS = 50;
    private int stared;

    public FacelessEntity(EntityType<? extends FacelessEntity> type, Level level) {
        super(type, level);
        this.setSilent(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 64.0F, 1.0F));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        Player player = level.getNearestPlayer(this, 64.0);
        if (player == null || player.isSpectator()) {
            this.stared = 0;
            return;
        }
        if (player.distanceTo(this) < NEAR) {
            this.vanish(level);
            return;
        }
        Vec3 toward = this.getEyePosition().subtract(player.getEyePosition()).normalize();
        boolean watched = player.getViewVector(1.0F).dot(toward) > STARE_COS && player.hasLineOfSight(this);
        this.stared = watched ? this.stared + 1 : Math.max(0, this.stared - 2);
        if (this.stared > STARE_TICKS) {
            this.vanish(level);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        this.vanish(level);
        return false;
    }

    /** Sometimes a figure leaves something behind where it stood: a memory of this place. */
    private void vanish(ServerLevel level) {
        if (this.getRandom().nextFloat() < 0.3F) {
            net.minecraft.world.item.ItemStack memory = new net.minecraft.world.item.ItemStack(com.eltavine.oneirgeo.registry.OneirgeoItems.MEMORY_FRAGMENT);
            memory.set(com.eltavine.oneirgeo.registry.OneirgeoComponents.CHAPTER, com.eltavine.oneirgeo.story.Chapter.of(level.dimension()));
            this.spawnAtLocation(level, memory);
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 12, 0.25, 0.6, 0.25, 0.0);
        level.playSound(null, this.blockPosition(), OneirgeoSounds.ENTITY_FIGURE.value(), SoundSource.HOSTILE, 0.5F, 0.7F);
        this.discard();
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
