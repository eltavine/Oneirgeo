package com.eltavine.oneirgeo.survival;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.registry.OneirgeoAttachments;
import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import com.eltavine.oneirgeo.space.SpaceQuery;
import com.eltavine.oneirgeo.world.OneirgeoDimensions;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * The abstract dimensions grow little to live on, so the dream provides: every few minutes something
 * appears in your pocket, drawn from {@code oneirgeo:supply/<dimension>}. Nothing reaches a closed room.
 */
public final class Supplies {
    private static final int STEP = 20;

    private Supplies() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % STEP == 0) {
                tick(server);
            }
        });
    }

    public static ResourceKey<LootTable> tableFor(ServerLevel level) {
        String name = OneirgeoDimensions.isDream(level.dimension()) ? "dream" : level.dimension().identifier().getPath();
        return ResourceKey.create(Registries.LOOT_TABLE, Oneirgeo.id("supply/" + name));
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerLevel level = player.level();
            int interval = level.getGameRules().get(OneirgeoRules.SUPPLY_INTERVAL);
            if (interval <= 0 || player.isSpectator() || player.isCreative()) {
                continue;
            }
            int remaining = player.getAttachedOrElse(OneirgeoAttachments.SUPPLY_TIMER, interval) - STEP;
            if (remaining > 0) {
                player.setAttached(OneirgeoAttachments.SUPPLY_TIMER, remaining);
                continue;
            }
            player.setAttached(OneirgeoAttachments.SUPPLY_TIMER, interval);
            if (SpaceQuery.isTrap(level, player.position())) {
                player.sendOverlayMessage(Component.translatable("oneirgeo.supply.none"));
                continue;
            }
            deliver(player);
        }
    }

    /** Rolls the supply table of the player's dimension and puts the result in their inventory. */
    public static boolean deliver(ServerPlayer player) {
        ServerLevel level = player.level();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(tableFor(level));
        if (table == LootTable.EMPTY) {
            table = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, Oneirgeo.id("supply/default")));
        }
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .create(LootContextParamSets.GIFT);
        List<ItemStack> items = table.getRandomItems(params);
        if (items.isEmpty()) {
            return false;
        }
        for (ItemStack stack : items) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false, Prediction.SERVER_ONLY);
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), OneirgeoSounds.SUPPLY.value(), SoundSource.PLAYERS, 0.5F, 1.0F);
        player.sendOverlayMessage(Component.translatable("oneirgeo.supply.arrived"));
        return true;
    }
}
