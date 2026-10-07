package com.eltavine.oneirgeo.world.gen;

import com.eltavine.oneirgeo.registry.OneirgeoAttachments;
import com.eltavine.oneirgeo.space.Box;
import com.eltavine.oneirgeo.space.ChunkSpaceData;
import com.eltavine.oneirgeo.world.gen.landmark.Landmark;
import com.eltavine.oneirgeo.world.gen.landmark.LandmarkField;
import com.eltavine.oneirgeo.world.gen.landmark.LandmarkFields;
import com.eltavine.oneirgeo.world.gen.scene.DecorationContext;
import com.eltavine.oneirgeo.world.gen.scene.Scene;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import com.eltavine.oneirgeo.world.gen.scene.SceneInfo;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.jspecify.annotations.Nullable;

/**
 * Generates a dimension from a {@link DimensionLayout}: height layers, Voronoi regions inside each
 * layer, one {@link Scene} per region, then cross-layer landmarks on top.
 */
public final class OneirgeoChunkGenerator extends ChunkGenerator {
    public static final MapCodec<OneirgeoChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            DimensionLayout.CODEC.fieldOf("layout").forGetter(OneirgeoChunkGenerator::layout)
    ).apply(i, i.stable(OneirgeoChunkGenerator::new)));

    private final Holder<DimensionLayout> layout;
    private final OneirgeoBiomeSource layeredBiomes;

    public OneirgeoChunkGenerator(Holder<DimensionLayout> layout) {
        this(layout, new OneirgeoBiomeSource(layout));
    }

    private OneirgeoChunkGenerator(Holder<DimensionLayout> layout, OneirgeoBiomeSource biomeSource) {
        super(biomeSource);
        this.layout = layout;
        this.layeredBiomes = biomeSource;
    }

    public Holder<DimensionLayout> layout() {
        return this.layout;
    }

    public LayoutSampler sampler() {
        return this.layeredBiomes.sampler();
    }

    public LandmarkField landmarks() {
        return LandmarkFields.get(this.layout.value().landmarks());
    }

    public long landmarkSeed() {
        return this.sampler().layerSeed(-1);
    }

    /** The sampler of a level using this generator, seeded even before the level generated anything. */
    public LayoutSampler sampler(ServerLevel level) {
        return this.sampler(level.getChunkSource().randomState());
    }

    private LayoutSampler sampler(RandomState randomState) {
        this.layeredBiomes.initSeed(randomState.seed());
        return this.layeredBiomes.sampler();
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSets, RandomState randomState, long legacyLevelSeed) {
        this.layeredBiomes.initSeed(randomState.seed());
        return super.createState(structureSets, randomState, legacyLevelSeed);
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess protoChunk) {
        this.layeredBiomes.initSeed(randomState.seed());
        return super.createBiomes(randomState, blender, structureManager, protoChunk);
    }

    @Override
    public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState, StructureManager structureManager,
                                                       BiomeManager biomeManager, @Nullable WorldGenRegion carverBiomeRegion, Set<Holder<Biome>> possibleBiomes) {
        long started = System.nanoTime();
        long startedCpu = GenerationStats.cpuNanos();
        this.fill(chunk, this.sampler(randomState));
        GenerationStats.record(System.nanoTime() - started, GenerationStats.cpuNanos() - startedCpu);
        return CompletableFuture.completedFuture(chunk);
    }

    /** Writes the terrain, seams and gravity zones of one chunk. */
    private void fill(ChunkAccess chunk, LayoutSampler sampler) {
        ChunkWriter writer = new ChunkWriter(chunk);
        ChunkSpaceData.Builder space = new ChunkSpaceData.Builder(writer.minBlockX(), writer.minBlockZ());
        LevelChunkSection[] sections = chunk.getSections();
        for (LevelChunkSection section : sections) {
            section.acquire();
        }
        try {
            for (int l = 0; l < sampler.layerCount(); l++) {
                DimensionLayout.Layer layer = sampler.layer(l);
                if (layer.maxY() < chunk.getMinY() || layer.minY() > chunk.getMaxY()) {
                    continue;
                }
                LayoutSampler.Region[] regions = regions(sampler, l, writer.minBlockX(), writer.minBlockZ());
                for (int entry : distinctEntries(regions)) {
                    boolean[] owned = ownership(regions, entry);
                    SceneInfo info = sampler.info(l, firstOwned(regions, owned));
                    sampler.scene(l, entry).generate(new SceneContext(info, sampler, writer, space, owned, regions));
                }
            }
            this.placeLandmarks(sampler, writer, space);
        } finally {
            for (LevelChunkSection section : sections) {
                section.release();
            }
        }
        ChunkSpaceData data = space.build();
        if (!data.isEmpty()) {
            chunk.setAttached(OneirgeoAttachments.SPACE, data);
        }
        Heightmap.primeHeightmaps(chunk, EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG));
    }

    /**
     * Development measurement: the terrain of a square of chunks, written again and again on this
     * thread into fresh proto chunks. Returns the fastest round's milliseconds per chunk, which other
     * load on the machine disturbs far less than timings taken during normal generation.
     */
    public double benchmarkTerrain(ServerLevel level, int chunkX, int chunkZ, int size, int rounds) {
        LayoutSampler sampler = this.sampler(level.getChunkSource().randomState());
        double best = Double.MAX_VALUE;
        for (int round = 0; round < rounds; round++) {
            List<ProtoChunk> chunks = new java.util.ArrayList<>();
            for (int dx = 0; dx < size; dx++) {
                for (int dz = 0; dz < size; dz++) {
                    chunks.add(new ProtoChunk(new ChunkPos(chunkX + dx, chunkZ + dz), UpgradeData.EMPTY, level, level.palettedContainerFactory(), null));
                }
            }
            long started = System.nanoTime();
            for (ProtoChunk chunk : chunks) {
                this.fill(chunk, sampler);
            }
            best = Math.min(best, (System.nanoTime() - started) / 1.0E6 / chunks.size());
        }
        return best;
    }

    private void placeLandmarks(LayoutSampler sampler, ChunkWriter writer, ChunkSpaceData.Builder space) {
        LandmarkField field = this.landmarks();
        long seed = sampler.layerSeed(-1);
        int minX = writer.minBlockX();
        int minZ = writer.minBlockZ();
        List<Landmark> landmarks = field.collect(seed, minX, minZ, minX + 15, minZ + 15);
        for (Landmark landmark : landmarks) {
            int reach = landmark.horizontalReach() + 4;
            int y0 = Math.max(landmark.y(), writer.minY());
            int y1 = Math.min(landmark.maxY(), writer.maxY());
            int lx0 = Math.max(landmark.x() - reach - minX, 0);
            int lx1 = Math.min(landmark.x() + reach - minX, 15);
            int lz0 = Math.max(landmark.z() - reach - minZ, 0);
            int lz1 = Math.min(landmark.z() + reach - minZ, 15);
            boolean round = landmark.shape() != Landmark.Shape.BOX;
            double reachSq = (double) reach * reach;
            for (int lz = lz0; lz <= lz1; lz++) {
                for (int lx = lx0; lx <= lx1; lx++) {
                    int x = minX + lx;
                    int z = minZ + lz;
                    if (round) {
                        double dx = x + 0.5 - landmark.x();
                        double dz = z + 0.5 - landmark.z();
                        if (dx * dx + dz * dz > reachSq) {
                            continue;
                        }
                    }
                    for (int y = y0; y <= y1; y++) {
                        BlockState state = field.blockAt(landmark, seed, x, y, z);
                        if (state != null) {
                            writer.set(lx, y, lz, state);
                        }
                    }
                }
            }
            if (landmark.visible()) {
                space.protectSolid(new Box(landmark.x() - reach, landmark.y(), landmark.z() - reach,
                        landmark.x() + reach + 1, landmark.maxY() + 1, landmark.z() + reach + 1));
            }
            field.collectSpace(landmark, seed, space);
        }
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        LayoutSampler sampler = this.sampler();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        for (int l = 0; l < sampler.layerCount(); l++) {
            LayoutSampler.Region[] regions = regions(sampler, l, minX, minZ);
            for (int entry : distinctEntries(regions)) {
                boolean[] owned = ownership(regions, entry);
                SceneInfo info = sampler.info(l, firstOwned(regions, owned));
                sampler.scene(l, entry).decorate(new DecorationContext(level, chunk, info, sampler, owned));
            }
        }
    }

    private static LayoutSampler.Region[] regions(LayoutSampler sampler, int layer, int minX, int minZ) {
        LayoutSampler.Region[] regions = new LayoutSampler.Region[256];
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                regions[lx | lz << 4] = sampler.region(layer, minX + lx, minZ + lz);
            }
        }
        return regions;
    }

    private static int[] distinctEntries(LayoutSampler.Region[] regions) {
        return Arrays.stream(regions).mapToInt(LayoutSampler.Region::entryIndex).distinct().toArray();
    }

    private static boolean[] ownership(LayoutSampler.Region[] regions, int entry) {
        boolean[] owned = new boolean[256];
        for (int i = 0; i < 256; i++) {
            owned[i] = regions[i].entryIndex() == entry;
        }
        return owned;
    }

    private static LayoutSampler.Region firstOwned(LayoutSampler.Region[] regions, boolean[] owned) {
        for (int i = 0; i < 256; i++) {
            if (owned[i]) {
                return regions[i];
            }
        }
        return regions[0];
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
    }

    @Override
    public int getGenDepth() {
        DimensionLayout value = this.layout.value();
        List<DimensionLayout.Layer> layers = value.layers();
        return layers.getLast().maxY() - layers.getFirst().minY() + 1;
    }

    @Override
    public int getSeaLevel() {
        return this.layout.value().seaLevel();
    }

    @Override
    public int getMinY() {
        return this.layout.value().layers().getFirst().minY();
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor heightAccessor) {
        return this.layout.value().spawnY();
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        int surface = this.sampler(randomState).surfaceY(x, z);
        return surface == Integer.MIN_VALUE ? heightAccessor.getMinY() : surface + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        int surface = this.sampler(randomState).surfaceY(x, z);
        BlockState[] column = new BlockState[heightAccessor.getHeight()];
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < column.length; i++) {
            int y = heightAccessor.getMinY() + i;
            column[i] = surface != Integer.MIN_VALUE && y <= surface && y > surface - 4 ? stone : air;
        }
        return new NoiseColumn(heightAccessor.getMinY(), column);
    }

    @Override
    public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext samplerContext) {
        LayoutSampler sampler = this.sampler(randomState);
        int layer = sampler.layerIndex(feetPos.getY());
        if (layer < 0) {
            result.add("Oneirgeo: between layers");
            return;
        }
        LayoutSampler.Region region = sampler.region(layer, feetPos.getX(), feetPos.getZ());
        result.add("Oneirgeo layer " + sampler.layer(layer).name() + " scene " + sampler.entry(layer, region.entryIndex()).scene()
                + " cell " + region.cellX() + "," + region.cellZ());
    }
}
