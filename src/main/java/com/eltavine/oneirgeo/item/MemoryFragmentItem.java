package com.eltavine.oneirgeo.item;

import com.eltavine.oneirgeo.registry.OneirgeoComponents;
import com.eltavine.oneirgeo.story.Chapter;
import com.eltavine.oneirgeo.story.Story;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * A scrap of the dreamer's memory. Hold it close and the next thing about the place it was found in
 * comes back, in order; the dream journal keeps what came back.
 */
public class MemoryFragmentItem extends Item {
    public MemoryFragmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            Chapter chapter = stack.getOrDefault(OneirgeoComponents.CHAPTER, Chapter.of(level.dimension()));
            if (Story.remember(serverPlayer, chapter)) {
                stack.consume(1, player);
            }
            player.getCooldowns().addCooldown(stack, 20);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        Chapter chapter = stack.get(OneirgeoComponents.CHAPTER);
        if (chapter != null) {
            builder.accept(Component.translatable("oneirgeo.fragment.of", Component.translatable(chapter.titleKey())).withColor(chapter.colour()));
        }
    }
}
