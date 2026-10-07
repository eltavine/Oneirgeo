package com.eltavine.oneirgeo.entity;

import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import com.eltavine.oneirgeo.survival.Lucidity;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Something tall and black that keeps its distance and keeps up. It does not move while you look at
 * it, follows you through folded space, and only comes closer when your mind is nearly gone.
 */
public class StalkerEntity extends Monster {
    private static final double KEEP_AWAY = 14.0;
    private static final double CATCH_UP = 22.0;
    private static final double LOSE = 160.0;
    private static final float HUNTS_BELOW = 0.2F;
    private static final double WATCHED_COS = 0.93;
    private @Nullable UUID quarry;
    private int stepCooldown;

    public StalkerEntity(EntityType<? extends StalkerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 96.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    public void follow(ServerPlayer player) {
        this.quarry = player.getUUID();
    }

    public boolean follows(ServerPlayer player) {
        return player.getUUID().equals(this.quarry);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.quarry == null && level.getNearestPlayer(this, 96.0) instanceof ServerPlayer nearest) {
            this.quarry = nearest.getUUID();
        }
        ServerPlayer player = this.quarry == null ? null : level.getServer().getPlayerList().getPlayer(this.quarry);
        if (player == null || player.level() != level || player.isSpectator() || player.distanceTo(this) > LOSE) {
            this.discard();
            return;
        }
        this.getLookControl().setLookAt(player, 30.0F, 30.0F);
        double distance = player.distanceTo(this);
        Vec3 toward = this.getEyePosition().subtract(player.getEyePosition()).normalize();
        boolean watched = player.getViewVector(1.0F).dot(toward) > WATCHED_COS && player.hasLineOfSight(this);
        if (watched) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
            return;
        }
        boolean hunting = Lucidity.get(player) < HUNTS_BELOW;
        if (hunting || distance > CATCH_UP) {
            this.getNavigation().moveTo(player, hunting ? 1.25 : 1.0);
            if (hunting && distance < 2.2) {
                this.doHurtTarget(level, player);
            }
        } else if (distance < KEEP_AWAY) {
            this.getNavigation().stop();
        }
        if (this.getNavigation().isInProgress() && --this.stepCooldown <= 0) {
            this.stepCooldown = 14;
            level.playSound(null, this.blockPosition(), OneirgeoSounds.ENTITY_STALKER.value(), SoundSource.HOSTILE, 0.6F, 0.6F);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }
}
