package com.eltavine.oneirgeo.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * Time in the End does not keep: the sky drifts, then jumps hours ahead, stops dead, races, or runs
 * backwards for a while. Only runs while someone is there to see it.
 */
public final class EndTime {
    private static final long FLOOR = 24000L * 40;

    private enum Mode {
        FLOW,
        STOP,
        RUSH,
        REVERSE
    }

    private static Mode mode = Mode.FLOW;
    private static int remaining = 1200;

    private EndTime() {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Holder<WorldClock> clock = clock(server);
            server.clockManager().setPaused(clock, false);
            server.clockManager().setRate(clock, 1.0F);
            mode = Mode.FLOW;
            remaining = 1200;
        });
        ServerTickEvents.END_SERVER_TICK.register(EndTime::tick);
    }

    private static Holder<WorldClock> clock(MinecraftServer server) {
        return server.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.THE_END);
    }

    private static void tick(MinecraftServer server) {
        ServerLevel end = server.getLevel(Level.END);
        if (end == null || end.players().isEmpty() || !end.dimensionTypeRegistration().is(OneirgeoDimensions.END_TYPE)
                || !server.getGlobalGameRules().get(GameRules.ADVANCE_TIME)) {
            return;
        }
        ServerClockManager clocks = server.clockManager();
        Holder<WorldClock> clock = clock(server);
        if (mode == Mode.REVERSE && server.getTickCount() % 2 == 0) {
            clocks.addTicks(clock, -10);
        }
        if (--remaining > 0) {
            return;
        }
        switch (mode) {
            case STOP, REVERSE -> clocks.setPaused(clock, false);
            case RUSH -> clocks.setRate(clock, 1.0F);
            default -> {
            }
        }
        RandomSource random = end.getRandom();
        int roll = random.nextInt(100);
        if (roll < 30) {
            mode = Mode.FLOW;
            remaining = 600 + random.nextInt(2400);
        } else if (roll < 50) {
            clocks.addTicks(clock, 2000 + random.nextInt(16000));
            mode = Mode.FLOW;
            remaining = 400 + random.nextInt(1200);
        } else if (roll < 70) {
            clocks.setPaused(clock, true);
            mode = Mode.STOP;
            remaining = 300 + random.nextInt(1500);
        } else if (roll < 85) {
            clocks.setRate(clock, 10.0F + random.nextInt(30));
            mode = Mode.RUSH;
            remaining = 100 + random.nextInt(300);
        } else {
            if (clocks.getInstance(clock).totalTicks() < FLOOR) {
                clocks.setTotalTicks(clock, FLOOR + random.nextInt(24000));
            }
            clocks.setPaused(clock, true);
            mode = Mode.REVERSE;
            remaining = 200 + random.nextInt(800);
        }
    }
}
