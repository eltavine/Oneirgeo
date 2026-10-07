package com.eltavine.oneirgeo.item;

import com.eltavine.oneirgeo.registry.OneirgeoComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
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

/** One of the father's tapes, labelled in his handwriting. Played on a television. */
public class VhsTapeItem extends Item {
    public VhsTapeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendOverlayMessage(Component.translatable("oneirgeo.tape.no_player").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        String tape = stack.get(OneirgeoComponents.TAPE);
        if (tape != null) {
            builder.accept(Component.translatable("oneirgeo.tape." + tape + ".label").withStyle(ChatFormatting.GRAY));
        }
    }
}
