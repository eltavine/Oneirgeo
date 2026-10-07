package com.eltavine.oneirgeo.block;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Grows a column of {@link UpdraftBlock} into the open air above it and removes it again when broken. */
public class UpdraftVentBlock extends Block {
    public static final int MAX_HEIGHT = 384;

    public UpdraftVentBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 4);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState updraft = OneirgeoBlocks.UPDRAFT.defaultBlockState();
        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (int i = 1; i <= MAX_HEIGHT && cursor.getY() < level.getMaxY(); i++) {
            cursor.move(0, 1, 0);
            BlockState current = level.getBlockState(cursor);
            if (current.is(OneirgeoBlocks.UPDRAFT)) {
                continue;
            }
            if (!current.isAir()) {
                break;
            }
            level.setBlock(cursor, updraft, Block.UPDATE_CLIENTS);
        }
        level.scheduleTick(pos, this, 200);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (int i = 1; i <= MAX_HEIGHT; i++) {
            cursor.move(0, 1, 0);
            if (!level.getBlockState(cursor).is(OneirgeoBlocks.UPDRAFT)) {
                break;
            }
            level.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
