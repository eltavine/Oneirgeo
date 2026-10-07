package com.eltavine.oneirgeo.client.dev;

import com.eltavine.oneirgeo.Oneirgeo;
import com.eltavine.oneirgeo.client.space.ClientGravity;
import com.eltavine.oneirgeo.client.story.JournalScreen;
import com.eltavine.oneirgeo.story.Chapter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Visual smoke test ({@code runClientGameTest}): creates a world with the default preset, visits a
 * landmark of every dimension as a spectator and saves a screenshot of each under
 * {@code run/gametest/screenshots}. Shots without coordinates use {@code /oneirgeo dim}.
 */
public class OneirgeoClientGameTest implements FabricClientGameTest {
    private record Shot(String name, String dimension, double x, double y, double z, float yaw, float pitch) {
    }

    private static final Shot[] SHOTS = {
            new Shot("overworld_spawn", "minecraft:overworld", 0.5, 66.0, 0.5, -90.0F, 4.0F),
            new Shot("overworld_vault", "minecraft:overworld", -1.5, 29.0, -3.5, 90.0F, 35.0F),
            new Shot("overworld_web", "minecraft:overworld", 180.5, 240.0, 140.5, 30.0F, 10.0F),
            new Shot("overworld_web_deep", "minecraft:overworld", -420.5, -900.0, 310.5, 200.0F, -15.0F),
            new Shot("nether_chimney", "minecraft:the_nether", 76.5, -1205.0, 40.5, 90.0F, -25.0F),
            new Shot("nether_city", "minecraft:the_nether", 900.5, 1860.0, 900.5, 30.0F, 10.0F),
            new Shot("end_arrival", "minecraft:the_end", 100.5, 50.0, 0.5, 90.0F, 0.0F),
            new Shot("end_sky", "minecraft:the_end", 100.5, 50.0, 0.5, 140.0F, -45.0F),
            new Shot("end_observatory", "minecraft:the_end", -136.5, 27.0, -44.5, 135.0F, -10.0F),
            new Shot("end_stair", "minecraft:the_end", 452.5, 90.0, 92.5, 135.0F, 0.0F),
            new Shot("end_sea", "minecraft:the_end", 8.5, -990.0, -25.5, -39.0F, -4.0F),
            new Shot("end_geometry", "minecraft:the_end", 0.5, 1300.0, 0.5, 45.0F, 0.0F),
            new Shot("mirror_sea", "oneirgeo:mirror_sea", 2.5, 0.1, 2.5, 30.0F, 6.0F),
            new Shot("mirror_reflection", "oneirgeo:mirror_sea", 2.5, 0.1, 2.5, 30.0F, 50.0F),
            new Shot("poolrooms", "oneirgeo:poolrooms", Double.NaN, 0.0, 0.0, 45.0F, 4.0F),
            new Shot("backrooms", "oneirgeo:backrooms", Double.NaN, 0.0, 0.0, 45.0F, 0.0F),
    };

    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> client.options.renderDistance().set(6));
        try (TestSingleplayerContext world = context.worldBuilder()
                .setUseConsistentSettings(false)
                .adjustSettings(settings -> settings.setSeed("20261006"))
                .create()) {
            world.getServer().runCommand("gamemode spectator @a");
            for (Shot shot : SHOTS) {
                if (Double.isNaN(shot.x())) {
                    world.getServer().runCommand("execute as @a run oneirgeo dim " + shot.dimension());
                    world.getServer().runCommand(String.format(java.util.Locale.ROOT, "execute as @a at @s run tp @s ~ ~ ~ %.1f %.1f",
                            shot.yaw(), shot.pitch()));
                } else {
                    world.getServer().runCommand(String.format(java.util.Locale.ROOT, "execute in %s run tp @a %.1f %.1f %.1f %.1f %.1f",
                            shot.dimension(), shot.x(), shot.y(), shot.z(), shot.yaw(), shot.pitch()));
                }
                context.waitTicks(20);
                world.getConnection().waitForChunksRender(true, 3 * 1200);
                context.waitTicks(60);
                context.takeScreenshot("oneirgeo_" + shot.name());
                float flip = context.computeOnClient(client -> ClientGravity.amount(1.0F));
                String where = context.computeOnClient(client -> client.player == null ? "-"
                        : client.player.blockPosition().toShortString() + " in " + client.level.getBlockState(client.player.blockPosition().above()));
                Oneirgeo.LOGGER.info("[gametest] {}: camera flip {}, at {}", shot.name(), flip, where);
            }
            world.getServer().runCommand("gamemode creative @a");
            world.getServer().runCommand("execute in minecraft:overworld run tp @a 0.5 66.0 0.5 90.0 8.0");
            world.getConnection().waitForChunksRender(true, 3 * 1200);
            for (String summon : new String[]{"oneirgeo:faceless -14 65 3", "oneirgeo:stalker -10 65 -4", "oneirgeo:mimic -4 65 1", "minecraft:cow -6 65 5"}) {
                world.getServer().runCommand("execute in minecraft:overworld run summon " + summon);
            }
            context.waitTicks(60);
            context.takeScreenshot("oneirgeo_apparitions");
            world.getServer().runCommand("execute as @a run oneirgeo story remember observatory 7");
            context.waitTicks(100);
            context.takeScreenshot("oneirgeo_story_tape");
            context.waitTicks(360);
            context.runOnClient(client -> JournalScreen.open(Chapter.OBSERVATORY));
            context.waitTicks(20);
            context.takeScreenshot("oneirgeo_story_journal");
            context.runOnClient(client -> client.gui.setScreen(null));
        }
    }
}
