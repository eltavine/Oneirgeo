package com.eltavine.oneirgeo.world.gen.scene.neural;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.scene.DecorationContext;
import com.eltavine.oneirgeo.world.gen.scene.Frame;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * Where everyone wakes up: a lump of tissue in the fog whose top was cut flat long ago, with a ruined
 * house, telephone poles that carry nothing, and a ladder down to a hollow where an End portal waits.
 */
final class SpawnHub {
    static final int PLATFORM = 64;
    private static final float CX = 0.5F;
    private static final float CY = 40.5F;
    private static final float CZ = 0.5F;
    private static final int RADIUS = 34;
    /** Other tissue keeps out of this cylinder around the hub, between these heights. */
    private static final int CLEAR = 36;
    private static final int CLEAR_BELOW = 0;
    private static final int CLEAR_ABOVE = 104;
    /** The sky above the platform stays open all the way up, so spawning lands on the platform. */
    private static final int SHAFT = 22;
    private static final int HOUSE_X = 16;
    private static final int HOUSE_Z = 0;
    private static final int LADDER_X = -14;
    private static final int LADDER_Z = -6;
    private static final int VAULT_X = -6;
    private static final int VAULT_Y = 36;
    private static final int VAULT_Z = -3;
    private static final int VAULT_RADIUS = 11;
    private static final int VAULT_FLOOR = 28;
    /**
     * The stair under the vault: a five by four ring of steps around a pillar, fourteen steps a turn,
     * red carpet on every step. The fourth turn is the second again, for anyone who does not
     * remember everything yet; the others reach the room where the clocks stopped.
     */
    static final int STAIR_X = VAULT_X + 3;
    static final int STAIR_Z = VAULT_Z + 4;
    static final int STAIR_TOP = VAULT_FLOOR - 1;
    static final int STEPS = 14;
    private static final int TURNS = 6;
    static final int STAIR_BOTTOM = STAIR_TOP - STEPS * TURNS + 1;
    private static final int[][] RING = {{0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 0}, {4, 1}, {4, 2}, {4, 3}, {3, 3}, {2, 3}, {1, 3}, {0, 3}, {0, 2}, {0, 1}};
    private static final int ROOM_X0 = STAIR_X - 10;
    private static final int ROOM_X1 = STAIR_X - 1;
    private static final int ROOM_Z0 = STAIR_Z - 4;
    private static final int ROOM_Z1 = STAIR_Z + 5;
    private static final int ROOM_FLOOR = STAIR_BOTTOM;
    private static final int ROOM_CEILING = ROOM_FLOOR + 6;
    /** Where whoever stands is inside the room where the clocks stopped. */
    static final com.eltavine.oneirgeo.space.Box ROOM = new com.eltavine.oneirgeo.space.Box(ROOM_X0 + 1, ROOM_FLOOR + 1, ROOM_Z0 + 1, ROOM_X1, ROOM_CEILING, ROOM_Z1);
    private static final NeuralNetwork.Tier HUB = new NeuralNetwork.Tier(-1, 1, 1.0, RADIUS, RADIUS, 8, 9, 3, 2.6F, 0.14F, 0, 0, 0.0, -1, 0, 0);
    private static final Map<Long, Neuron> HUBS = new ConcurrentHashMap<>();

    private SpawnHub() {
    }

    static Neuron hub(long seed) {
        return HUBS.computeIfAbsent(seed, s -> Neuron.grow(new Neuron.Header(CX, CY, CZ, RADIUS, Hash.of(s, 0x50A)), HUB, s, 0, 0, 0, false));
    }

    /** True where tissue of other neurons must not grow. */
    static boolean excluded(int x, int y, int z) {
        int d2 = x * x + z * z;
        return (d2 <= CLEAR * CLEAR && y >= CLEAR_BELOW && y <= CLEAR_ABOVE) || (d2 <= SHAFT * SHAFT && y > PLATFORM);
    }

    /** True where the hub itself is cut away: above its flat top. */
    static boolean cut(int x, int y, int z) {
        return y > PLATFORM && x * x + z * z <= (RADIUS + 2) * (RADIUS + 2);
    }

    static boolean near(SceneContext ctx) {
        return ctx.intersectsChunk(-RADIUS - 4, -RADIUS - 4, RADIUS + 4, RADIUS + 4);
    }

    static void build(SceneContext ctx, long seed) {
        if (!near(ctx)) {
            return;
        }
        vault(ctx, seed);
        stair(ctx);
        room(ctx);
        ladder(ctx);
        house(ctx, seed);
        poles(ctx);
    }

    private static void vault(SceneContext ctx, long seed) {
        for (int x = VAULT_X - VAULT_RADIUS; x <= VAULT_X + VAULT_RADIUS; x++) {
            for (int z = VAULT_Z - VAULT_RADIUS; z <= VAULT_Z + VAULT_RADIUS; z++) {
                if (!ctx.intersectsChunk(x, z, x, z)) {
                    continue;
                }
                for (int y = VAULT_Y - VAULT_RADIUS; y <= VAULT_Y + VAULT_RADIUS; y++) {
                    double dx = x + 0.5 - (VAULT_X + 0.5);
                    double dy = y + 0.5 - (VAULT_Y + 0.5);
                    double dz = z + 0.5 - (VAULT_Z + 0.5);
                    double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (d > VAULT_RADIUS) {
                        continue;
                    }
                    if (y == VAULT_FLOOR) {
                        ctx.place(x, y, z, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                    } else if (y > VAULT_FLOOR) {
                        ctx.place(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        for (int[] at : new int[][]{{VAULT_X + 7, VAULT_Z}, {VAULT_X - 7, VAULT_Z}, {VAULT_X, VAULT_Z + 7}, {VAULT_X, VAULT_Z - 7}}) {
            ctx.place(at[0], VAULT_Y + 2, at[1], OneirgeoBlocks.SYNAPSE.defaultBlockState());
        }
    }

    private static int ringIndex(int dx, int dz) {
        for (int i = 0; i < RING.length; i++) {
            if (RING[i][0] == dx && RING[i][1] == dz) {
                return i;
            }
        }
        return -1;
    }

    private static void stair(SceneContext ctx) {
        BlockState wall = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState step = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        BlockState carpet = Blocks.CARPET.pick(DyeColor.RED).defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int dx = -1; dx <= 5; dx++) {
            for (int dz = -1; dz <= 4; dz++) {
                int x = STAIR_X + dx;
                int z = STAIR_Z + dz;
                if (!ctx.intersectsChunk(x, z, x, z)) {
                    continue;
                }
                int ring = ringIndex(dx, dz);
                for (int y = STAIR_BOTTOM - 1; y <= VAULT_FLOOR + 2; y++) {
                    BlockState state;
                    if (ring < 0) {
                        boolean outer = dx == -1 || dx == 5 || dz == -1 || dz == 4;
                        state = y >= VAULT_FLOOR + (outer ? 0 : 1) ? null : wall;
                    } else if (y == STAIR_BOTTOM - 1) {
                        state = wall;
                    } else {
                        int below = STAIR_TOP - ring - y;
                        state = below >= 0 && below % STEPS == 0 ? step : (below >= -1 && (below + 1) % STEPS == 0 && y <= STAIR_TOP + 1 - ring ? carpet : air);
                        if (y > VAULT_FLOOR) {
                            state = null;
                        }
                    }
                    if (state != null) {
                        ctx.place(x, y, z, state);
                    }
                }
            }
        }
        int loopFeet = STAIR_TOP - STEPS * 3 + 1;
        ctx.seam(com.eltavine.oneirgeo.space.SeamVolume.translate(
                new com.eltavine.oneirgeo.space.Box(STAIR_X, loopFeet, STAIR_Z, STAIR_X + 1, loopFeet + 1, STAIR_Z + 1), 0, STEPS, 0, null).lettingAwakePass());
        ctx.protectSolid(new com.eltavine.oneirgeo.space.Box(STAIR_X - 1, STAIR_BOTTOM - 1, STAIR_Z - 1, STAIR_X + 6, VAULT_FLOOR, STAIR_Z + 5));
    }

    /** At the foot of the stair: a hospital room with the light on, and every clock at 3:17. */
    private static void room(SceneContext ctx) {
        BlockState plaster = OneirgeoBlocks.FADED_PLASTER.defaultBlockState();
        BlockState floor = Blocks.CONCRETE.pick(DyeColor.WHITE).defaultBlockState();
        BlockState ceiling = OneirgeoBlocks.CEILING_TILE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int x = ROOM_X0; x <= ROOM_X1; x++) {
            for (int z = ROOM_Z0; z <= ROOM_Z1; z++) {
                if (!ctx.intersectsChunk(x, z, x, z)) {
                    continue;
                }
                boolean edge = x == ROOM_X0 || x == ROOM_X1 || z == ROOM_Z0 || z == ROOM_Z1;
                for (int y = ROOM_FLOOR - 1; y <= ROOM_CEILING + 1; y++) {
                    BlockState state;
                    if (y <= ROOM_FLOOR) {
                        state = floor;
                    } else if (y >= ROOM_CEILING) {
                        state = x == (ROOM_X0 + ROOM_X1) / 2 && z == (ROOM_Z0 + ROOM_Z1) / 2 && y == ROOM_CEILING
                                ? OneirgeoBlocks.FLUORESCENT_LIGHT.defaultBlockState() : ceiling;
                    } else {
                        state = edge ? plaster : air;
                    }
                    if (x == ROOM_X1 && z == STAIR_Z + 1 && (y == ROOM_FLOOR + 1 || y == ROOM_FLOOR + 2)) {
                        state = air;
                    }
                    ctx.place(x, y, z, state);
                }
            }
        }
        int bx = ROOM_X0 + 4;
        int bz = STAIR_Z;
        int y = ROOM_FLOOR + 1;
        ctx.place(bx, y, bz, furniture(OneirgeoBlocks.HOSPITAL_BED, Direction.EAST));
        ctx.place(bx, y, bz - 1, furniture(OneirgeoBlocks.HEART_MONITOR, Direction.EAST));
        ctx.place(bx + 1, y, bz - 1, furniture(OneirgeoBlocks.IV_STAND, Direction.EAST));
        ctx.place(bx, y, bz + 1, furniture(OneirgeoBlocks.WAITING_CHAIR, Direction.NORTH));
        ctx.place(bx + 4, y, bz, furniture(OneirgeoBlocks.TELEVISION, Direction.WEST));
        ctx.place(bx + 4, y, bz + 2, furniture(OneirgeoBlocks.TELEPHONE, Direction.WEST));
        ctx.place(bx, y + 2, ROOM_Z0 + 1, furniture(OneirgeoBlocks.STOPPED_CLOCK, Direction.SOUTH));
        ctx.protectSolid(new com.eltavine.oneirgeo.space.Box(ROOM_X0, ROOM_FLOOR - 1, ROOM_Z0, ROOM_X1 + 1, ROOM_CEILING + 2, ROOM_Z1 + 1));
    }

    private static BlockState furniture(net.minecraft.world.level.block.Block block, Direction facing) {
        return block.defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, facing);
    }

    private static void ladder(SceneContext ctx) {
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
        BlockState backing = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
        for (int y = VAULT_FLOOR + 1; y <= PLATFORM; y++) {
            ctx.place(LADDER_X, y, LADDER_Z, ladder);
            ctx.place(LADDER_X, y, LADDER_Z - 1, backing);
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                boolean opening = dz == 1 && dx == 0;
                if ((dx != 0 || dz != 0) && !opening) {
                    ctx.place(LADDER_X + dx, PLATFORM + 1, LADDER_Z + dz, backing);
                }
            }
        }
    }

    /** The house of the old spawn, sunk into the fog: holes in the walls and roof, glass gone, door ajar. */
    private static void house(SceneContext ctx, long seed) {
        Frame f = new Frame(HOUSE_X, PLATFORM + 1, HOUSE_Z, Direction.WEST);
        int base = PLATFORM + 1;
        BlockState plaster = OneirgeoBlocks.FADED_PLASTER.defaultBlockState();
        BlockState log = Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int a = -3; a <= 3; a++) {
            for (int b = -4; b <= 4; b++) {
                int x = f.worldX(a, b);
                int z = f.worldZ(a, b);
                for (int y = base - 4; y < base; y++) {
                    ctx.place(x, y, z, Blocks.STONE_BRICKS.defaultBlockState());
                }
                ctx.place(x, base, z, Hash.chance(Hash.of(seed, x, base, z), 0.12) ? Blocks.COARSE_DIRT.defaultBlockState() : planks);
                boolean wall = Math.abs(a) == 3 || Math.abs(b) == 4;
                for (int y = base + 1; y <= base + 4; y++) {
                    BlockState state = air;
                    if (wall) {
                        boolean corner = Math.abs(a) == 3 && Math.abs(b) == 4;
                        boolean hole = !corner && y >= base + 2 && Hash.chance(Hash.of(seed, x, y, z, 0x401E), 0.16);
                        state = corner ? log : (hole ? air : plaster);
                    }
                    ctx.place(x, y, z, state);
                }
            }
        }
        Direction out = f.world(Direction.SOUTH);
        BlockState door = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, out.getOpposite()).setValue(DoorBlock.OPEN, true);
        ctx.place(f.worldX(0, 4), base + 1, f.worldZ(0, 4), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        ctx.place(f.worldX(0, 4), base + 2, f.worldZ(0, 4), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        Direction rightward = f.world(Direction.EAST);
        for (int b = -5; b <= 5; b++) {
            for (int a = -4; a <= 4; a++) {
                int x = f.worldX(a, b);
                int z = f.worldZ(a, b);
                int roofY = base + 5 + (4 - Math.abs(a));
                boolean collapsed = Hash.chance(Hash.of(seed, a, b, 0x2007), 0.22) || (b >= 1 && b <= 3 && a >= -1 && a <= 2);
                BlockState roof;
                if (a == 0) {
                    roof = Blocks.DARK_OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
                    roofY = base + 9;
                } else {
                    Direction facing = a < 0 ? rightward : rightward.getOpposite();
                    roof = Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing);
                }
                ctx.place(x, roofY, z, collapsed ? air : roof);
                if (Math.abs(b) == 4 && Math.abs(a) <= 3) {
                    for (int y = base + 5; y < roofY; y++) {
                        ctx.place(x, y, z, Hash.chance(Hash.of(seed, x, y, z, 0x6AB), 0.2) ? air : plaster);
                    }
                } else if (Math.abs(b) < 4 && Math.abs(a) <= 3) {
                    for (int y = base + 5; y < roofY; y++) {
                        ctx.place(x, y, z, air);
                    }
                }
            }
        }
        for (int y = base + 5; y <= base + 7; y++) {
            ctx.place(f.worldX(2, -2), y, f.worldZ(2, -2), Blocks.BRICKS.defaultBlockState());
        }
        ctx.place(f.worldX(1, 2), base + 1, f.worldZ(1, 2), Blocks.DARK_OAK_PLANKS.defaultBlockState());
        ctx.place(f.worldX(-1, 3), base + 1, f.worldZ(-1, 3), Blocks.CARPET.pick(DyeColor.GRAY).defaultBlockState());
    }

    /** Three poles that once carried a line; one snapped, wires hang from the others into the fog. */
    private static void poles(SceneContext ctx) {
        BlockState log = Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState beam = Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        BlockState chain = Blocks.IRON_CHAIN.defaultBlockState();
        int[][] standing = {{-18, 10}, {-4, 20}};
        for (int[] p : standing) {
            for (int y = PLATFORM + 1; y <= PLATFORM + 8; y++) {
                ctx.place(p[0], y, p[1], log);
            }
            for (int dx = -2; dx <= 2; dx++) {
                ctx.place(p[0] + dx, PLATFORM + 7, p[1], beam);
            }
            for (int y = PLATFORM + 3; y <= PLATFORM + 6; y++) {
                ctx.place(p[0] - 2, y, p[1], chain);
            }
        }
        for (int y = PLATFORM + 1; y <= PLATFORM + 3; y++) {
            ctx.place(6, y, -19, log);
        }
        for (int dx = 1; dx <= 5; dx++) {
            ctx.place(6 + dx, PLATFORM + 1, -19, beam);
        }
    }

    static void decorate(DecorationContext ctx) {
        Frame f = new Frame(HOUSE_X, PLATFORM + 1, HOUSE_Z, Direction.WEST);
        int base = PLATFORM + 1;
        Direction back = f.world(Direction.NORTH);
        BlockPos foot = new BlockPos(f.worldX(-2, -2), base + 1, f.worldZ(-2, -2));
        if (ctx.canPlace(foot) && ctx.canPlace(foot.relative(back))) {
            ctx.bed(foot, back, DyeColor.GRAY);
        }
        BlockPos chest = new BlockPos(f.worldX(2, -3), base + 1, f.worldZ(2, -3));
        if (ctx.canPlace(chest)) {
            ctx.chest(chest, f.world(Direction.SOUTH), BuiltInLootTables.VILLAGE_PLAINS_HOUSE);
        }
        ctx.furniture(new BlockPos(f.worldX(1, 2), base + 2, f.worldZ(1, 2)), OneirgeoBlocks.TELEPHONE, f.world(Direction.SOUTH));
        ctx.furniture(new BlockPos(f.worldX(2, 1), base + 1, f.worldZ(2, 1)), OneirgeoBlocks.TELEVISION, f.world(Direction.WEST));
        ctx.furniture(new BlockPos(f.worldX(0, 1), base + 1, f.worldZ(0, 1)), OneirgeoBlocks.WAITING_CHAIR, f.world(Direction.EAST));
        ctx.furniture(new BlockPos(f.worldX(0, -3), base + 3, f.worldZ(0, -3)), OneirgeoBlocks.STOPPED_CLOCK, f.world(Direction.SOUTH));
        portal(ctx, VAULT_X, VAULT_FLOOR, VAULT_Z);
        BlockPos vaultChest = new BlockPos(VAULT_X - 5, VAULT_FLOOR + 1, VAULT_Z + 2);
        if (ctx.canPlace(vaultChest)) {
            ctx.chest(vaultChest, Direction.EAST, BuiltInLootTables.STRONGHOLD_LIBRARY);
        }
    }

    /** An already lit End portal: twelve frames with eyes around nine portal blocks. */
    private static void portal(DecorationContext ctx, int cx, int y, int cz) {
        BlockState frame = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.HAS_EYE, true);
        for (int i = -1; i <= 1; i++) {
            ctx.set(new BlockPos(cx + i, y, cz - 2), frame.setValue(EndPortalFrameBlock.FACING, Direction.SOUTH));
            ctx.set(new BlockPos(cx + i, y, cz + 2), frame.setValue(EndPortalFrameBlock.FACING, Direction.NORTH));
            ctx.set(new BlockPos(cx - 2, y, cz + i), frame.setValue(EndPortalFrameBlock.FACING, Direction.EAST));
            ctx.set(new BlockPos(cx + 2, y, cz + i), frame.setValue(EndPortalFrameBlock.FACING, Direction.WEST));
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ctx.set(new BlockPos(cx + dx, y, cz + dz), Blocks.END_PORTAL.defaultBlockState());
            }
        }
    }
}
