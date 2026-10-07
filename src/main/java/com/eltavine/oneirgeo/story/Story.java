package com.eltavine.oneirgeo.story;

import com.eltavine.oneirgeo.command.OneirgeoCommands;
import com.eltavine.oneirgeo.network.RecordingPayload;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.eltavine.oneirgeo.registry.OneirgeoAttachments;
import com.eltavine.oneirgeo.registry.OneirgeoComponents;
import com.eltavine.oneirgeo.registry.OneirgeoItems;
import com.eltavine.oneirgeo.registry.OneirgeoSounds;
import com.eltavine.oneirgeo.survival.Lucidity;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The dreamer's story as each player remembers it. Memory fragments bring back the entries of a
 * chapter one by one; a complete chapter plays the father's tape of it; all six open the stairs
 * under the house down to the room where the clocks stopped.
 */
public final class Story {
    /** The observatory entry that carries the numbers from the back of the star map. */
    public static final int SECRET_ENTRY = 4;

    private Story() {
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(RecordingPayload.TYPE, RecordingPayload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> giveJournal(handler.getPlayer()));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 == 5) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    visitFinalRoom(player);
                }
            }
        });
        OneirgeoCommands.extend(root -> root.then(Commands.literal("story")
                .executes(ctx -> {
                    StoryProgress progress = get(ctx.getSource().getPlayerOrException());
                    ctx.getSource().sendSuccess(() -> Component.literal("story " + progress), false);
                    return progress.total();
                })
                .then(Commands.literal("remember").then(Commands.argument("chapter", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(java.util.Arrays.stream(Chapter.values()).map(Chapter::id), builder))
                        .executes(ctx -> remember(ctx, 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, Chapter.ENTRIES))
                                .executes(ctx -> remember(ctx, IntegerArgumentType.getInteger(ctx, "count"))))))
                .then(Commands.literal("play").then(Commands.argument("tape", StringArgumentType.word()).executes(ctx -> {
                    play(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "tape"));
                    return 1;
                })))
                .then(Commands.literal("reset").executes(ctx -> {
                    set(ctx.getSource().getPlayerOrException(), StoryProgress.EMPTY.with(StoryProgress.JOURNAL));
                    return 1;
                }))));
    }

    private static int remember(CommandContext<CommandSourceStack> ctx, int count) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String id = StringArgumentType.getString(ctx, "chapter");
        for (Chapter chapter : Chapter.values()) {
            if (chapter.id().equals(id)) {
                int done = 0;
                while (done < count && remember(player, chapter)) {
                    done++;
                }
                return done;
            }
        }
        return 0;
    }

    public static StoryProgress get(Player player) {
        return player.getAttachedOrElse(OneirgeoAttachments.STORY, StoryProgress.EMPTY);
    }

    static void set(ServerPlayer player, StoryProgress progress) {
        player.setAttached(OneirgeoAttachments.STORY, progress);
    }

    /** Everyone wakes with a notebook in their pocket, once. */
    private static void giveJournal(ServerPlayer player) {
        StoryProgress progress = get(player);
        if (progress.has(StoryProgress.JOURNAL)) {
            return;
        }
        set(player, progress.with(StoryProgress.JOURNAL));
        ItemStack journal = new ItemStack(OneirgeoItems.DREAM_JOURNAL);
        if (!player.getInventory().add(journal)) {
            player.drop(journal, false, Prediction.SERVER_ONLY);
        }
        player.sendSystemMessage(Component.translatable("oneirgeo.journal.found").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    /**
     * Brings back the next entry of a chapter. False when the whole chapter is already remembered,
     * and the fragment should be kept.
     */
    public static boolean remember(ServerPlayer player, Chapter chapter) {
        StoryProgress progress = get(player);
        int entry = progress.remembered(chapter);
        if (entry >= Chapter.ENTRIES) {
            player.sendOverlayMessage(Component.translatable("oneirgeo.fragment.known", Component.translatable(chapter.titleKey()))
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            return false;
        }
        StoryProgress next = progress.remember(chapter);
        if (chapter == Chapter.OBSERVATORY && entry == SECRET_ENTRY) {
            Optional<BlockPos> box = TimeCapsule.box(player.level().getServer());
            if (box.isPresent()) {
                next = next.withSecret(box.get().getX(), box.get().getZ());
            }
        }
        set(player, next);
        Component text = entry(chapter, entry, next).copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        player.sendSystemMessage(Component.translatable(chapter.titleKey()).withColor(chapter.colour()).append(Component.literal("  ")).append(text));
        player.sendOverlayMessage(text);
        player.level().playSound(null, player.blockPosition(), OneirgeoSounds.WHISPER.value(), SoundSource.PLAYERS, 0.5F, 0.75F);
        Lucidity.add(player, 0.04F);
        if (next.complete(chapter)) {
            complete(player, chapter, next);
        }
        return true;
    }

    /** The words of an entry; the star map one carries the numbers from its back, once remembered. */
    public static Component entry(Chapter chapter, int entry, StoryProgress progress) {
        if (chapter == Chapter.OBSERVATORY && entry == SECRET_ENTRY) {
            return progress.has(StoryProgress.SECRET)
                    ? Component.translatable(chapter.entryKey(entry), progress.secretX(), progress.secretZ())
                    : Component.translatable(chapter.entryKey(entry), "?", "?");
        }
        return Component.translatable(chapter.entryKey(entry));
    }

    private static void complete(ServerPlayer player, Chapter chapter, StoryProgress progress) {
        player.sendSystemMessage(Component.translatable("oneirgeo.story.complete", Component.translatable(chapter.titleKey()))
                .withColor(chapter.colour()));
        play(player, chapter.id());
        ItemStack tape = new ItemStack(OneirgeoItems.VHS_TAPE);
        tape.set(OneirgeoComponents.TAPE, chapter.id());
        if (!player.getInventory().add(tape)) {
            player.drop(tape, false, Prediction.SERVER_ONLY);
        }
        if (progress.allComplete()) {
            player.sendSystemMessage(Component.translatable("oneirgeo.story.all").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    /**
     * Whoever remembers everything and walks down the whole stair finds the room where the clocks
     * stopped; the first time, the last tape plays. Nothing ends: the dream goes on around them.
     */
    public static boolean visitFinalRoom(ServerPlayer player) {
        if (player.level().dimension() != net.minecraft.world.level.Level.OVERWORLD
                || !com.eltavine.oneirgeo.world.gen.scene.neural.NeuralWebScene.finalRoom().contains(player.getX(), player.getY(), player.getZ())) {
            return false;
        }
        StoryProgress progress = get(player);
        if (!progress.allComplete() || progress.has(StoryProgress.FINAL)) {
            return false;
        }
        set(player, progress.with(StoryProgress.FINAL));
        play(player, "final");
        player.sendSystemMessage(Component.translatable("oneirgeo.story.final").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        Lucidity.set(player, 1.0F);
        return true;
    }

    /** Plays a recording on the player's camcorder screen. */
    public static void play(ServerPlayer player, String tape) {
        if (ServerPlayNetworking.canSend(player, RecordingPayload.TYPE)) {
            ServerPlayNetworking.send(player, new RecordingPayload(tape));
        }
    }

    public static void flag(ServerPlayer player, int flag) {
        set(player, get(player).with(flag));
    }
}
