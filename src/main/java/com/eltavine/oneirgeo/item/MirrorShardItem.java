package com.eltavine.oneirgeo.item;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.world.MirrorPortals;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** A sliver of something that reflected too much. Pressed into an empty mirror frame, it fills it. */
public class MirrorShardItem extends Item {
    public MirrorShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        if (!level.getBlockState(clicked).is(OneirgeoBlocks.MIRROR_FRAME)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos inside = clicked.relative(context.getClickedFace());
        if (!MirrorPortals.tryLight(level, inside)) {
            return InteractionResult.FAIL;
        }
        level.playSound(null, inside, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, 0.6F);
        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
