package com.eltavine.oneirgeo.command;

import com.eltavine.oneirgeo.registry.OneirgeoAttachments;
import com.eltavine.oneirgeo.space.ChunkSpaceData;
import com.eltavine.oneirgeo.space.SeamVolume;
import com.eltavine.oneirgeo.survival.Lucidity;
import com.eltavine.oneirgeo.survival.Supplies;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.eltavine.oneirgeo.world.SafeSpot;
import com.eltavine.oneirgeo.world.gen.DimensionLayout;
import com.eltavine.oneirgeo.world.gen.GenerationStats;
import com.eltavine.oneirgeo.world.gen.LayoutSampler;
import com.eltavine.oneirgeo.world.gen.OneirgeoChunkGenerator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;

/** Developer and operator tools: {@code /oneirgeo where|stats|dim|layer|seams}. */
public final class OneirgeoCommands {
    private static final SimpleCommandExceptionType NOT_LAYERED = new SimpleCommandExceptionType(Component.translatable("commands.oneirgeo.not_layered"));
    private static final SimpleCommandExceptionType NO_LAYER = new SimpleCommandExceptionType(Component.translatable("commands.oneirgeo.no_layer"));

    private static final List<SubCommand> EXTENSIONS = new ArrayList<>();

    /** Lets other systems (lucidity, supply...) hang their own subcommands under /oneirgeo. */
    public interface SubCommand {
        void attach(LiteralArgumentBuilder<CommandSourceStack> root);
    }

    private OneirgeoCommands() {
    }

    public static void extend(SubCommand subCommand) {
        EXTENSIONS.add(subCommand);
    }

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("oneirgeo")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("where").executes(OneirgeoCommands::where))
                .then(Commands.literal("stats").executes(OneirgeoCommands::stats))
                .then(Commands.literal("seams").executes(OneirgeoCommands::seams))
                .then(Commands.literal("dim").then(Commands.argument("dimension", DimensionArgument.dimension()).executes(OneirgeoCommands::dim)))
                .then(Commands.literal("layer").then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(layerNames(ctx.getSource().getLevel()), builder))
                        .executes(OneirgeoCommands::layer)))
                .then(Commands.literal("lucidity")
                        .executes(ctx -> {
                            float value = Lucidity.get(ctx.getSource().getPlayerOrException());
                            ctx.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT, "lucidity %.3f", value)), false);
                            return Math.round(value * 100);
                        })
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0.0F, 1.0F)).executes(ctx -> {
                            Lucidity.set(ctx.getSource().getPlayerOrException(), FloatArgumentType.getFloat(ctx, "value"));
                            return 1;
                        })))
                .then(Commands.literal("supply").executes(ctx -> Supplies.deliver(ctx.getSource().getPlayerOrException()) ? 1 : 0));
        for (SubCommand extension : EXTENSIONS) {
            extension.attach(root);
        }
        dispatcher.register(root);
    }

    private static List<String> layerNames(ServerLevel level) {
        if (level.getChunkSource().getGenerator() instanceof OneirgeoChunkGenerator generator) {
            return generator.layout().value().layers().stream().map(DimensionLayout.Layer::name).toList();
        }
        return List.of();
    }

    private static OneirgeoChunkGenerator generator(ServerLevel level) throws CommandSyntaxException {
        if (level.getChunkSource().getGenerator() instanceof OneirgeoChunkGenerator generator) {
            return generator;
        }
        throw NOT_LAYERED.create();
    }

    private static int where(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.level();
        OneirgeoChunkGenerator generator = generator(level);
        LayoutSampler sampler = generator.sampler();
        BlockPos pos = player.blockPosition();
        int layer = sampler.layerIndex(pos.getY());
        String biome = level.getBiome(pos).unwrapKey().map(k -> k.identifier().toString()).orElse("?");
        if (layer < 0) {
            ctx.getSource().sendSuccess(() -> Component.literal("between layers, biome " + biome), false);
            return 0;
        }
        LayoutSampler.Region region = sampler.region(layer, pos.getX(), pos.getZ());
        DimensionLayout.SceneEntry entry = sampler.entry(layer, region.entryIndex());
        LevelChunk chunk = level.getChunkAt(pos);
        ChunkSpaceData space = chunk.getAttachedOrElse(OneirgeoAttachments.SPACE, ChunkSpaceData.EMPTY);
        String text = String.format(Locale.ROOT, "layer %s, scene %s, cell %d,%d (edge %.0f), biome %s; seams %d, flipped %s, trap %s",
                sampler.layer(layer).name(), entry.scene(), region.cellX(), region.cellZ(), region.edgeDistance(), biome,
                space.seams().size(), space.isFlipped(player.getX(), player.getY(), player.getZ()), space.isTrap(player.getX(), player.getY(), player.getZ()));
        ctx.getSource().sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static int stats(CommandContext<CommandSourceStack> ctx) {
        String text = String.format(Locale.ROOT, "%d chunks generated, %.2f ms average (%.2f ms CPU), %.2f ms worst",
                GenerationStats.chunks(), GenerationStats.averageMillis(), GenerationStats.averageCpuMillis(), GenerationStats.worstMillis());
        ctx.getSource().sendSuccess(() -> Component.literal(text), false);
        return (int) GenerationStats.chunks();
    }

    private static int seams(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.level();
        int found = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                LevelChunk chunk = level.getChunk(player.chunkPosition().x() + dx, player.chunkPosition().z() + dz);
                ChunkSpaceData space = chunk.getAttachedOrElse(OneirgeoAttachments.SPACE, ChunkSpaceData.EMPTY);
                for (SeamVolume seam : space.seams()) {
                    found++;
                    String text = String.format(Locale.ROOT, "seam %s -> (%d,%d,%d) rot %d dir %d", seam.trigger(), seam.dx(), seam.dy(), seam.dz(), seam.rotation(), seam.direction());
                    ctx.getSource().sendSuccess(() -> Component.literal(text), false);
                }
            }
        }
        int total = found;
        ctx.getSource().sendSuccess(() -> Component.literal(total + " seams around you"), false);
        return found;
    }

    private static int dim(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel target = DimensionArgument.getDimension(ctx, "dimension");
        int spawnY = target.getChunkSource().getGenerator().getSpawnHeight(target);
        BlockPos base = target.getRespawnData().pos();
        int hint = SafeSpot.surfaceHint(target, base.getX(), spawnY, base.getZ());
        BlockPos feet = SafeSpot.findOrBuild(target, base.getX(), hint, base.getZ(), Blocks.GLASS.defaultBlockState());
        player.teleportTo(target, feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), true);
        return 1;
    }

    private static int layer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.level();
        OneirgeoChunkGenerator generator = generator(level);
        String name = StringArgumentType.getString(ctx, "name");
        DimensionLayout.Layer layer = generator.layout().value().layers().stream().filter(l -> l.name().equals(name)).findFirst()
                .orElseThrow(NO_LAYER::create);
        int x = player.getBlockX();
        int z = player.getBlockZ();
        int middle = (layer.minY() + layer.maxY()) / 2;
        int hint = SafeSpot.surfaceHint(level, x, Math.min(layer.maxY() - 2, Math.max(layer.minY() + 2, middle)), z);
        if (hint < layer.minY() || hint > layer.maxY()) {
            hint = middle;
        }
        BlockPos feet = SafeSpot.findOrBuild(level, x, hint, z, Blocks.GLASS.defaultBlockState());
        player.teleportTo(level, feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), true);
        return 1;
    }
}
