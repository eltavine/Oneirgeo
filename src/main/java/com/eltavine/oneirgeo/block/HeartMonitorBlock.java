package com.eltavine.oneirgeo.block;

import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** A monitor counting somebody's heartbeats, slowly, with a green line that never quite goes flat. */
public class HeartMonitorBlock extends FurnitureBlock {
    public HeartMonitorBlock(Properties properties) {
        super(properties, box(2, 0, 4, 14, 11, 13), "oneirgeo.monitor.use");
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.6F) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, OneirgeoSounds.MONITOR_BEEP.value(), SoundSource.BLOCKS,
                    0.35F, 1.0F, false);
        }
    }
}
