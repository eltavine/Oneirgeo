package com.eltavine.oneirgeo.entity;

import com.eltavine.oneirgeo.registry.OneirgeoItems;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A barrel like every other barrel, until you reach for it. Then it opens, and hops after you, and
 * bites. Left alone long enough it settles down and is a barrel again.
 */
public class MimicEntity extends Monster {
    private static final EntityDataAccessor<Boolean> AWAKE = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double WAKE_DISTANCE = 2.6;
    private static final int SETTLE_AFTER = 200;
    private int calm;
    private int hop;

    public MimicEntity(EntityType<? extends MimicEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(AWAKE, false);
    }

    public boolean isAwake() {
        return this.entityData.get(AWAKE);
    }

    private void wake(ServerLevel level) {
        if (!this.isAwake()) {
            this.entityData.set(AWAKE, true);
            level.playSound(null, this.blockPosition(), SoundEvents.BARREL_OPEN, SoundSource.HOSTILE, 1.0F, 0.6F);
        }
        this.calm = 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        Player player = level.getNearestPlayer(this, 16.0);
        boolean reachable = player != null && !player.isSpectator() && !player.isCreative();
        if (!this.isAwake()) {
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
            if (reachable && player.distanceTo(this) < WAKE_DISTANCE) {
                this.wake(level);
            }
            return;
        }
        if (!reachable) {
            if (++this.calm > SETTLE_AFTER) {
                this.entityData.set(AWAKE, false);
                this.snapTo(Mth.floor(this.getX()) + 0.5, this.getY(), Mth.floor(this.getZ()) + 0.5, 0.0F, 0.0F);
                level.playSound(null, this.blockPosition(), SoundEvents.BARREL_CLOSE, SoundSource.HOSTILE, 1.0F, 0.6F);
            }
            return;
        }
        this.calm = 0;
        this.getLookControl().setLookAt(player, 30.0F, 30.0F);
        if (this.onGround() && --this.hop <= 0) {
            this.hop = 12 + this.random.nextInt(8);
            Vec3 toward = player.position().subtract(this.position()).multiply(1.0, 0.0, 1.0).normalize();
            this.setDeltaMovement(toward.x * 0.42, 0.42, toward.z * 0.42);
            this.needsSync = true;
        }
        if (player.distanceTo(this) < 1.7) {
            this.doHurtTarget(level, player);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        this.wake(level);
        return super.hurtServer(level, source, damage);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (this.random.nextFloat() < 0.5F) {
            this.spawnAtLocation(level, new ItemStack(OneirgeoItems.MEMORY_FRAGMENT));
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOOD_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOOD_BREAK;
    }

    @Override
    public boolean isPushable() {
        return this.isAwake();
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }
}
