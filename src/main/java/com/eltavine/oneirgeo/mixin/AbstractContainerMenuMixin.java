package com.eltavine.oneirgeo.mixin;

import com.eltavine.oneirgeo.item.BelongingItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Belongings cannot be thrown out of a container screen, or dropped by clicking outside it while carried. */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Shadow
    @Final
    public NonNullList<Slot> slots;
    @Shadow
    private int quickcraftStatus;

    @Shadow
    public abstract ItemStack getCarried();

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void oneirgeo$holdOnToBelongings(int slotIndex, int buttonNum, ContainerInput input, Player player, CallbackInfo ci) {
        // While dragging across slots, any other click only ends the drag.
        if (this.quickcraftStatus != 0) {
            return;
        }
        boolean outside = slotIndex == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE && (buttonNum == 0 || buttonNum == 1)
                && (input == ContainerInput.PICKUP || input == ContainerInput.QUICK_MOVE);
        boolean thrown = input == ContainerInput.THROW && slotIndex >= 0 && this.getCarried().isEmpty();
        ItemStack stack = outside ? this.getCarried() : thrown ? this.slots.get(slotIndex).getItem() : ItemStack.EMPTY;
        if (BelongingItem.holdOnTo(player, stack)) {
            ci.cancel();
        }
    }
}
