package com.eltavine.oneirgeo.client.config;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Locale;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

/** {@code /oneirgeofx}: adjust the client config without leaving the game. */
public final class ConfigCommand {
    private ConfigCommand() {
    }

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> dispatcher.register(ClientCommands.literal("oneirgeofx")
                .then(toggle("effects", (c, v) -> c.effects = v))
                .then(toggle("safe_mode", (c, v) -> c.safeMode = v))
                .then(toggle("screen_text", (c, v) -> c.screenText = v))
                .then(toggle("far_silhouettes", (c, v) -> c.farSilhouettes = v))
                .then(toggle("wrong_sky", (c, v) -> c.wrongSky = v))
                .then(toggle("reverb", (c, v) -> c.reverb = v))
                .then(toggle("camcorder", (c, v) -> c.camcorder = v))
                .then(ClientCommands.literal("shake").then(ClientCommands.argument("value", FloatArgumentType.floatArg(0.0F, 2.0F))
                        .executes(ctx -> {
                            OneirgeoConfig.get().shake = FloatArgumentType.getFloat(ctx, "value");
                            return saved(ctx);
                        })))
                .then(ClientCommands.literal("intensity").then(ClientCommands.argument("value", FloatArgumentType.floatArg(0.0F, 2.0F))
                        .executes(ctx -> {
                            OneirgeoConfig.get().intensity = FloatArgumentType.getFloat(ctx, "value");
                            return saved(ctx);
                        })))
                .then(ClientCommands.literal("reload").executes(ctx -> {
                    OneirgeoConfig.load();
                    return saved(ctx);
                }))
                .executes(ConfigCommand::show)));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> toggle(String name, BiConsumer<OneirgeoConfig, Boolean> setter) {
        return ClientCommands.literal(name).then(ClientCommands.argument("value", BoolArgumentType.bool()).executes(ctx -> {
            setter.accept(OneirgeoConfig.get(), BoolArgumentType.getBool(ctx, "value"));
            return saved(ctx);
        }));
    }

    private static int saved(CommandContext<FabricClientCommandSource> ctx) {
        OneirgeoConfig.save();
        return show(ctx);
    }

    private static int show(CommandContext<FabricClientCommandSource> ctx) {
        OneirgeoConfig c = OneirgeoConfig.get();
        ctx.getSource().sendFeedback(Component.translatable("oneirgeo.config.summary",
                String.valueOf(c.effects), String.valueOf(c.safeMode), String.format(Locale.ROOT, "%.2f", c.intensity),
                String.valueOf(c.screenText), String.valueOf(c.farSilhouettes), String.valueOf(c.wrongSky), String.valueOf(c.reverb),
                String.valueOf(c.camcorder), String.format(Locale.ROOT, "%.2f", c.shake)));
        return 1;
    }
}
