package com.eltavine.oneirgeo.world.gen;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Running cost of terrain generation, reported by {@code /oneirgeo stats}: wall time, and the CPU
 * time of the generating thread, which other load on the machine hardly changes.
 */
public final class GenerationStats {
    private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();
    private static final AtomicLong CHUNKS = new AtomicLong();
    private static final AtomicLong NANOS = new AtomicLong();
    private static final AtomicLong CPU = new AtomicLong();
    private static final AtomicLong WORST = new AtomicLong();

    private GenerationStats() {
    }

    /** CPU time of the current thread so far, or 0 where the JVM cannot tell. */
    public static long cpuNanos() {
        return THREADS.isCurrentThreadCpuTimeSupported() ? THREADS.getCurrentThreadCpuTime() : 0L;
    }

    public static void record(long nanos, long cpuNanos) {
        CHUNKS.incrementAndGet();
        NANOS.addAndGet(nanos);
        CPU.addAndGet(cpuNanos);
        WORST.accumulateAndGet(nanos, Math::max);
    }

    public static long chunks() {
        return CHUNKS.get();
    }

    public static double averageMillis() {
        long chunks = CHUNKS.get();
        return chunks == 0 ? 0.0 : NANOS.get() / 1.0E6 / chunks;
    }

    public static double averageCpuMillis() {
        long chunks = CHUNKS.get();
        return chunks == 0 ? 0.0 : CPU.get() / 1.0E6 / chunks;
    }

    public static double worstMillis() {
        return WORST.get() / 1.0E6;
    }

    public static void reset() {
        CHUNKS.set(0);
        NANOS.set(0);
        CPU.set(0);
        WORST.set(0);
    }
}
