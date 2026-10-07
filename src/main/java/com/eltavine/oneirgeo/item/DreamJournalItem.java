package com.eltavine.oneirgeo.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** The notebook everyone wakes up with; it keeps every memory that came back, chapter by chapter. */
public class DreamJournalItem extends BelongingItem {
    /** Opens the journal on the client; set by the client initializer. */
    public static Runnable opener = () -> {
    };

    public DreamJournalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            opener.run();
        }
        return InteractionResult.SUCCESS;
    }
}
