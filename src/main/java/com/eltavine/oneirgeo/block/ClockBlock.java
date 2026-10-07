package com.eltavine.oneirgeo.block;

import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

/** A wall clock stopped at 3:17, like every clock in the house. Hung on the wall it was clicked on. */
public class ClockBlock extends FurnitureBlock {
    public ClockBlock(Properties properties) {
        super(properties, box(2, 2, 14, 14, 14, 16), "oneirgeo.clock.stopped");
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        return face.getAxis().isHorizontal() ? this.defaultBlockState().setValue(FACING, face) : super.getStateForPlacement(context);
    }

    /** For a moment the clock moves on to 3:18, and then it is 3:17 again. */
    public static void tickOnce(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, OneirgeoSounds.CLOCK_TICK.value(), SoundSource.BLOCKS, 0.7F, 1.0F);
    }
}
