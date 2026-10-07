package com.eltavine.oneirgeo.block;

import com.eltavine.oneirgeo.registry.OneirgeoComponents;
import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import com.eltavine.oneirgeo.story.Story;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A heavy old television. Put one of the father's tapes in and it plays on your camcorder screen;
 * with nothing in it, there is only snow, and sometimes a word in the snow.
 */
public class TelevisionBlock extends FurnitureBlock {
    public static final BooleanProperty PLAYING = BooleanProperty.create("playing");
    public static final int SNOW_LINES = 8;

    public TelevisionBlock(Properties properties) {
        super(properties, box(1, 0, 2, 15, 13, 15), null);
        this.registerDefaultState(this.defaultBlockState().setValue(PLAYING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PLAYING);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        String tape = stack.get(OneirgeoComponents.TAPE);
        if (tape == null) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            Story.play(serverPlayer, tape);
            this.switchOn(level, pos, state, 420);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            int line = serverPlayer.getRandom().nextInt(SNOW_LINES);
            serverPlayer.sendOverlayMessage(Component.translatable("oneirgeo.tv.snow." + line).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            level.playSound(null, pos, OneirgeoSounds.TAPE_STATIC.value(), SoundSource.BLOCKS, 0.5F, 1.0F);
            this.switchOn(level, pos, state, 50);
        }
        return InteractionResult.SUCCESS;
    }

    /** Turns the screen on for a while; mysteries use it to wake televisions nobody touched. */
    public void switchOn(Level level, BlockPos pos, BlockState state, int ticks) {
        level.setBlock(pos, state.setValue(PLAYING, true), Block.UPDATE_ALL);
        level.playSound(null, pos, OneirgeoSounds.TV_ON.value(), SoundSource.BLOCKS, 0.5F, 1.0F);
        level.scheduleTick(pos, this, ticks);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(PLAYING)) {
            level.setBlock(pos, state.setValue(PLAYING, false), Block.UPDATE_ALL);
        }
    }
}
