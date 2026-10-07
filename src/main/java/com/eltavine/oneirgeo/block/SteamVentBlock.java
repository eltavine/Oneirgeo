package com.eltavine.oneirgeo.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A grate in the boiler room floor that breathes out steam, and every few seconds bursts: whatever
 * stands on it is thrown up through the hole in the ceiling to the floor above. Client and server
 * agree on when, from the game time and the vent's position.
 */
public class SteamVentBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);
    private static final int PERIOD = 90;
    private static final int BURST = 10;

    public SteamVentBlock(Properties properties) {
        super(properties);
    }

    public static boolean bursting(Level level, BlockPos pos) {
        return bursting(level.getGameTime(), pos);
    }

    public static boolean bursting(long gameTime, BlockPos pos) {
        return Math.floorMod(gameTime + pos.asLong() * 31L, (long) PERIOD) < BURST;
    }

    /** What a burst does to whatever stands on the vent. */
    public static void launch(net.minecraft.world.entity.Entity entity) {
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x, Math.max(motion.y, 1.25), motion.z);
        entity.resetFallDistance();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (bursting(level, pos)) {
            launch(entity);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        boolean burst = bursting(level, pos);
        int puffs = burst ? 6 : (random.nextInt(4) == 0 ? 1 : 0);
        for (int i = 0; i < puffs; i++) {
            level.addAlwaysVisibleParticle(burst ? ParticleTypes.CLOUD : ParticleTypes.WHITE_SMOKE,
                    pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 0.8, pos.getZ() + 0.2 + random.nextDouble() * 0.6,
                    0.0, burst ? 0.45 + random.nextDouble() * 0.3 : 0.04, 0.0);
        }
    }
}
