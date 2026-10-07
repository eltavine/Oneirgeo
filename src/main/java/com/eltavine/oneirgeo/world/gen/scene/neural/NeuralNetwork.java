package com.eltavine.oneirgeo.world.gen.scene.neural;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Neurons at four scales, one candidate per cell of a cubic grid for each scale, filling all of
 * space. Neurons are grown once and shared by every chunk they reach.
 */
final class NeuralNetwork {
    /**
     * @param lengthFactor first dendrite length per block of soma radius
     * @param radiusFactor first dendrite radius per block of soma radius
     * @param loopChance   chance that the axon is an endless tube instead of a cable to a neighbour
     */
    record Tier(int index, int cell, double chance, int somaMin, int somaMax, int dendriteMin, int dendriteMax, int depth,
                float lengthFactor, float radiusFactor, int axonMin, int axonMax, double loopChance, int parent, int linkRange, int cacheLimit) {
        /** How far tissue of a neuron in this tier can reach from its cell. */
        int reach() {
            float dendrites = this.somaMax * this.lengthFactor * (this.depth >= 4 ? 2.6F : this.depth == 3 ? 2.2F : 1.7F);
            float axon = this.axonMax > 0 ? this.cell * 1.8F : 0.0F;
            float loop = this.loopChance > 0.0 ? Neuron.LOOP_PERIOD * (Neuron.LOOP_PERIODS + 1) : 0.0F;
            int strands = this.hasStrands() ? Neuron.STRAND_MAX : 0;
            return (int) Math.ceil(this.somaMax + Math.max(Math.max(dendrites, axon), Math.max(loop, this.linkRange))) + strands + 8;
        }

        /** Big neurons let threads hang from their branches; small ones are only soma, twigs and a link. */
        boolean hasStrands() {
            return this.axonMax > 0;
        }
    }

    /** Colossal, large, medium and small neurons; each links to the nearest neuron one size up, so the scales hang together. */
    static final Tier[] TIERS = {
            new Tier(0, 480, 0.5, 30, 64, 7, 10, 4, 1.9F, 0.17F, 3, 5, 0.25, -1, 0, 800),
            new Tier(1, 160, 0.7, 10, 24, 6, 9, 4, 2.0F, 0.22F, 2, 4, 0.15, 0, 400, 2000),
            new Tier(2, 64, 0.8, 4, 9, 5, 8, 3, 2.4F, 0.3F, 2, 3, 0.0, 1, 170, 6000),
            new Tier(3, 32, 0.7, 2, 4, 3, 5, 2, 3.0F, 0.4F, 0, 0, 0.0, 2, 40, 16000),
    };

    private final long seed;
    @SuppressWarnings("unchecked")
    private final ConcurrentHashMap<Long, Neuron>[] caches = new ConcurrentHashMap[TIERS.length];

    NeuralNetwork(long seed) {
        this.seed = seed;
        for (int i = 0; i < TIERS.length; i++) {
            this.caches[i] = new ConcurrentHashMap<>();
        }
    }

    long seed() {
        return this.seed;
    }

    Neuron neuron(Tier tier, int cx, int cy, int cz) {
        if (tier.cacheLimit() == 0) {
            return Neuron.build(this.seed, tier, cx, cy, cz);
        }
        ConcurrentHashMap<Long, Neuron> cache = this.caches[tier.index()];
        long key = ((long) cx & 0x1FFFFF) | ((long) cy & 0x1FFFFF) << 21 | ((long) cz & 0x1FFFFF) << 42;
        Neuron neuron = cache.get(key);
        if (neuron == null) {
            if (cache.size() > tier.cacheLimit()) {
                cache.clear();
            }
            neuron = cache.computeIfAbsent(key, k -> Neuron.build(this.seed, tier, cx, cy, cz));
        }
        return neuron;
    }

    /** Neurons currently cached, and the capsules they hold. */
    long[] cached() {
        long neurons = 0;
        long capsules = 0;
        for (ConcurrentHashMap<Long, Neuron> cache : this.caches) {
            for (Neuron neuron : cache.values()) {
                neurons++;
                capsules += neuron.capsuleCount();
            }
        }
        return new long[]{neurons, capsules};
    }

    /** Every neuron with tissue inside the block box (inclusive). */
    List<Neuron> collect(int x0, int y0, int z0, int x1, int y1, int z1) {
        List<Neuron> out = new ArrayList<>();
        for (Tier tier : TIERS) {
            int reach = tier.reach();
            int cell = tier.cell();
            int cx0 = Math.floorDiv(x0 - reach, cell);
            int cx1 = Math.floorDiv(x1 + reach, cell);
            int cy0 = Math.floorDiv(y0 - reach, cell);
            int cy1 = Math.floorDiv(y1 + reach, cell);
            int cz0 = Math.floorDiv(z0 - reach, cell);
            int cz1 = Math.floorDiv(z1 + reach, cell);
            for (int cx = cx0; cx <= cx1; cx++) {
                for (int cz = cz0; cz <= cz1; cz++) {
                    for (int cy = cy0; cy <= cy1; cy++) {
                        Neuron neuron = this.neuron(tier, cx, cy, cz);
                        if (neuron.intersects(x0, y0, z0, x1, y1, z1)) {
                            out.add(neuron);
                        }
                    }
                }
            }
        }
        return out;
    }
}
