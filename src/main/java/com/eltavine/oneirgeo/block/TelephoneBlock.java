package com.eltavine.oneirgeo.block;

import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import com.eltavine.oneirgeo.survival.Lucidity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A telephone on a side table. Now and then it rings for whoever is alone near it; pick it up and
 * somebody far away is talking, slower than they should.
 */
public class TelephoneBlock extends FurnitureBlock {
    public static final BooleanProperty RINGING = BooleanProperty.create("ringing");
    public static final int LINES = 10;
    private static final int RING_EVERY = 40;

    public TelephoneBlock(Properties properties) {
        super(properties, box(3, 0, 4, 13, 6, 12), null);
        this.registerDefaultState(this.defaultBlockState().setValue(RINGING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RINGING);
    }

    /** Starts ringing; it rings until answered or until whoever is calling gives up. */
    public void ring(ServerLevel level, BlockPos pos, BlockState state) {
        if (!state.getValue(RINGING)) {
            level.setBlock(pos, state.setValue(RINGING, true), Block.UPDATE_ALL);
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(RINGING)) {
            return;
        }
        if (random.nextFloat() < 0.08F) {
            level.setBlock(pos, state.setValue(RINGING, false), Block.UPDATE_ALL);
            return;
        }
        level.playSound(null, pos, OneirgeoSounds.TELEPHONE_RING.value(), SoundSource.BLOCKS, 0.8F, 1.0F);
        level.scheduleTick(pos, this, RING_EVERY);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (state.getValue(RINGING)) {
                level.setBlock(pos, state.setValue(RINGING, false), Block.UPDATE_ALL);
                int line = serverPlayer.getRandom().nextInt(LINES);
                serverPlayer.sendSystemMessage(Component.translatable("oneirgeo.phone." + line).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                level.playSound(null, pos, OneirgeoSounds.TELEPHONE_LINE.value(), SoundSource.BLOCKS, 0.4F, 1.0F);
                Lucidity.add(serverPlayer, 0.03F);
            } else {
                serverPlayer.sendOverlayMessage(Component.translatable("oneirgeo.phone.tone").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                level.playSound(null, pos, OneirgeoSounds.TELEPHONE_LINE.value(), SoundSource.BLOCKS, 0.25F, 1.4F);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
