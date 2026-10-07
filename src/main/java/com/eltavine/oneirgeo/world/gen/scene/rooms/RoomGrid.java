package com.eltavine.oneirgeo.world.gen.scene.rooms;

import com.eltavine.oneirgeo.registry.OneirgeoBlocks;
import com.eltavine.oneirgeo.space.Box;
import com.eltavine.oneirgeo.space.SeamVolume;
import com.eltavine.oneirgeo.util.Hash;
import com.eltavine.oneirgeo.world.gen.scene.DecorationContext;
import com.eltavine.oneirgeo.world.gen.scene.SceneContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * A three dimensional grid of rooms. Each floor is {@code floorHeight} tall: one floor row, the room,
 * then a ceiling row and a filler row. Walls run along the west and north edge of every cell and are
 * open, pierced by a doorway, or solid.
 * <p>
 * Two kinds of cell bend space. A <b>closed room</b> has one way in; every way out, including any
 * hole dug through its walls, floor or ceiling, is a seam back into the same room. A <b>corridor
 * run</b> replaces nine cells with one long hallway whose far end is never reached: a seam a little
 * way in sends you back by one period of its identical lamps.
 */
public final class RoomGrid {
    public enum Wall { OPEN, DOOR, SOLID }

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState FILLER = Blocks.STONE.defaultBlockState();
    public static final int RUN_CELLS = 9;

    private final int cell;
    private final int floorHeight;
    private final double openChance;
    private final double doorChance;
    private final double holeChance;
    private final double hiddenDoorChance;
    private final double closedChance;
    private final double chestChance;
    private final double runChance;
    private final int specialFloor;

    /** Everything decided for one cell of one floor. */
    public record Cell(int cellX, int cellZ, int floor, boolean closed, boolean hiddenDoor, boolean hole, boolean holeAbove,
                       int runStart, Wall west, int westDoor, Wall north, int northDoor) {
        public boolean inRun() {
            return this.runStart != Integer.MIN_VALUE;
        }
    }

    public RoomGrid(int cell, int floorHeight) {
        this(cell, floorHeight, 0.15, 0.55, 0.035, 0.012, 0.004, 0.03, 0.004, -13);
    }

    /**
     * @param specialFloor closed rooms, hidden doors and corridor runs only appear at or below this floor
     */
    public RoomGrid(int cell, int floorHeight, double openChance, double doorChance, double holeChance,
                    double hiddenDoorChance, double closedChance, double chestChance, double runChance, int specialFloor) {
        this.cell = cell;
        this.floorHeight = floorHeight;
        this.openChance = openChance;
        this.doorChance = doorChance;
        this.holeChance = holeChance;
        this.hiddenDoorChance = hiddenDoorChance;
        this.closedChance = closedChance;
        this.chestChance = chestChance;
        this.runChance = runChance;
        this.specialFloor = specialFloor;
    }

    public int cell() {
        return this.cell;
    }

    public int floorHeight() {
        return this.floorHeight;
    }

    private long cellHash(long seed, int floor, int cellX, int cellZ, int salt) {
        return Hash.of(seed, floor, cellX, cellZ, salt);
    }

    public boolean isClosed(long seed, int floor, int cellX, int cellZ) {
        return floor <= this.specialFloor && Hash.chance(this.cellHash(seed, floor, cellX, cellZ, 0xC105ED), this.closedChance);
    }

    private boolean isRunStart(long seed, int floor, int cellX, int cellZ) {
        return floor <= this.specialFloor && Hash.chance(this.cellHash(seed, floor, cellX, cellZ, 0x2ACE), this.runChance);
    }

    /** Start cell of the corridor run covering this cell, or {@code Integer.MIN_VALUE}. */
    public int runStart(long seed, int floor, int cellX, int cellZ) {
        for (int start = cellX; start > cellX - RUN_CELLS; start--) {
            if (this.isRunStart(seed, floor, start, cellZ)) {
                return start;
            }
        }
        return Integer.MIN_VALUE;
    }

    public boolean isClosedRoom(long seed, int x, int y, int z) {
        int floor = Math.floorDiv(y, this.floorHeight);
        int cellX = Math.floorDiv(x, this.cell);
        int cellZ = Math.floorDiv(z, this.cell);
        return this.isClosed(seed, floor, cellX, cellZ) && this.runStart(seed, floor, cellX, cellZ) == Integer.MIN_VALUE;
    }

    private boolean special(long seed, int floor, int cellX, int cellZ) {
        return this.isClosed(seed, floor, cellX, cellZ) || this.runStart(seed, floor, cellX, cellZ) != Integer.MIN_VALUE;
    }

    private boolean hole(long seed, int floor, int cellX, int cellZ) {
        return Hash.chance(this.cellHash(seed, floor, cellX, cellZ, 0x401E), this.holeChance) && !this.special(seed, floor, cellX, cellZ);
    }

    public Cell cellAt(long seed, int floor, int cellX, int cellZ) {
        int run = this.runStart(seed, floor, cellX, cellZ);
        boolean closed = run == Integer.MIN_VALUE && this.isClosed(seed, floor, cellX, cellZ);
        boolean hidden = run == Integer.MIN_VALUE && !closed && floor <= this.specialFloor
                && Hash.chance(this.cellHash(seed, floor, cellX, cellZ, 0xD002), this.hiddenDoorChance);
        boolean hole = run == Integer.MIN_VALUE && this.hole(seed, floor, cellX, cellZ);
        boolean holeAbove = run == Integer.MIN_VALUE && this.hole(seed, floor + 1, cellX, cellZ);

        boolean westClosed = this.isClosed(seed, floor, cellX - 1, cellZ) && this.runStart(seed, floor, cellX - 1, cellZ) == Integer.MIN_VALUE;
        boolean northClosed = this.isClosed(seed, floor, cellX, cellZ - 1) && this.runStart(seed, floor, cellX, cellZ - 1) == Integer.MIN_VALUE;
        Wall west;
        Wall north;
        if (closed) {
            west = Wall.DOOR;
            north = Wall.SOLID;
        } else {
            west = westClosed ? Wall.SOLID : this.rollWall(seed, floor, cellX, cellZ, true);
            north = northClosed || hidden ? Wall.SOLID : this.rollWall(seed, floor, cellX, cellZ, false);
        }
        int westDoor = Hash.range(this.cellHash(seed, floor, cellX, cellZ, 0xD0E), 2, this.cell - 4);
        int northDoor = Hash.range(this.cellHash(seed, floor, cellX, cellZ, 0xD0F), 2, this.cell - 4);
        return new Cell(cellX, cellZ, floor, closed, hidden, hole, holeAbove, run, west, westDoor, north, northDoor);
    }

    private Wall rollWall(long seed, int floor, int cellX, int cellZ, boolean west) {
        double roll = Hash.unit(this.cellHash(seed, floor, cellX, cellZ, west ? 0x3E57 : 0x4027));
        if (roll < this.openChance) {
            return Wall.OPEN;
        }
        return roll < this.openChance + this.doorChance ? Wall.DOOR : Wall.SOLID;
    }

    private boolean lit(long seed, Cell c, RoomStyle style) {
        return Hash.chance(this.cellHash(seed, c.floor(), c.cellX(), c.cellZ(), 0x11A), style.lightChance());
    }

    /** Generates one floor for every owned column of the chunk. */
    public void generateFloor(SceneContext ctx, long seed, int floor, RoomStyle style) {
        int c0x = Math.floorDiv(ctx.originX(), this.cell);
        int c0z = Math.floorDiv(ctx.originZ(), this.cell);
        int c1x = Math.floorDiv(ctx.originX() + 15, this.cell);
        int c1z = Math.floorDiv(ctx.originZ() + 15, this.cell);
        Cell[][] cells = new Cell[c1x - c0x + 1][c1z - c0z + 1];
        for (int cx = c0x; cx <= c1x; cx++) {
            for (int cz = c0z; cz <= c1z; cz++) {
                cells[cx - c0x][cz - c0z] = this.cellAt(seed, floor, cx, cz);
            }
        }
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                if (!ctx.owns(lx, lz)) {
                    continue;
                }
                int x = ctx.originX() + lx;
                int z = ctx.originZ() + lz;
                Cell c = cells[Math.floorDiv(x, this.cell) - c0x][Math.floorDiv(z, this.cell) - c0z];
                if (c.inRun()) {
                    this.corridorColumn(ctx, seed, lx, lz, x, z, c, style);
                } else {
                    this.roomColumn(ctx, seed, lx, lz, x, z, c, style);
                }
            }
        }
    }

    private void roomColumn(SceneContext ctx, long seed, int lx, int lz, int x, int z, Cell c, RoomStyle style) {
        int y0 = c.floor() * this.floorHeight;
        int u = Math.floorMod(x, this.cell);
        int v = Math.floorMod(z, this.cell);
        int ceilingY = y0 + this.floorHeight - 2;
        boolean inHole = u >= 5 && u <= 6 && v >= 4 && v <= 5;

        ctx.set(lx, y0, lz, c.hole() && inHole ? AIR : style.floor());
        Wall kind = Wall.OPEN;
        int along = 0;
        int doorAt = -100;
        if (u == 0 && v == 0) {
            kind = Wall.SOLID;
        } else if (u == 0) {
            kind = c.west();
            along = v;
            doorAt = c.westDoor();
            if (c.closed()) {
                doorAt = this.cell / 2;
            }
        } else if (v == 0) {
            kind = c.north();
            along = u;
            doorAt = c.northDoor();
        }
        for (int y = y0 + 1; y < ceilingY; y++) {
            BlockState state = AIR;
            if (kind == Wall.SOLID || (kind == Wall.DOOR && (along < doorAt || along > doorAt + 1 || y > y0 + 3))) {
                boolean fallen = !c.closed() && style.decay() > 0.0 && Hash.chance(Hash.of(seed, x, y, z, 0xDECA), style.decay());
                state = fallen ? AIR : (y == y0 + 1 ? style.trim() : style.wall());
            }
            ctx.set(lx, y, lz, state);
        }
        boolean light = u == this.cell / 2 && v == this.cell / 2 && this.lit(seed, c, style);
        boolean open = c.holeAbove() && inHole;
        ctx.set(lx, ceilingY, lz, open ? AIR : (light ? style.light() : style.ceiling()));
        ctx.set(lx, ceilingY + 1, lz, open ? AIR : FILLER);

        if (u == 5 && (v == 3 || v == 4) && (c.holeAbove() || c.hole())) {
            BlockState state = v == 3 ? style.wall() : Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
            int from = c.hole() ? y0 : y0 + 1;
            int to = c.holeAbove() ? ceilingY + 1 : y0 + 1;
            for (int y = from; y <= to; y++) {
                ctx.set(lx, y, lz, state);
            }
        }
        if (v == 1 && u == this.cell / 2 && c.hiddenDoor()) {
            this.door(ctx, lx, lz, y0, style.hiddenDoor(), Direction.SOUTH);
        }
        if (v == this.cell - 1 && u == this.cell / 2 && c.closed()) {
            this.door(ctx, lx, lz, y0, OneirgeoBlocks.CLOSED_DOOR.defaultBlockState(), Direction.NORTH);
        }
    }

    /** Corridor cross-section: three wide, four tall, lamps every half cell; the rest of the run is solid. */
    private void corridorColumn(SceneContext ctx, long seed, int lx, int lz, int x, int z, Cell c, RoomStyle style) {
        int y0 = c.floor() * this.floorHeight;
        int v = Math.floorMod(z, this.cell);
        int along = x - c.runStart() * this.cell;
        int ceilingY = y0 + this.floorHeight - 2;
        boolean inside = v >= 5 && v <= 7 && along < RUN_CELLS * this.cell;
        ctx.set(lx, y0, lz, style.floor());
        for (int y = y0 + 1; y <= y0 + 4; y++) {
            ctx.set(lx, y, lz, inside ? AIR : (y == y0 + 1 ? style.trim() : style.wall()));
        }
        boolean lamp = inside && v == 6 && Math.floorMod(along, 6) == 3;
        ctx.set(lx, y0 + 5, lz, lamp ? style.light() : (inside ? style.ceiling() : style.wall()));
        ctx.set(lx, ceilingY, lz, style.ceiling());
        ctx.set(lx, ceilingY + 1, lz, FILLER);
    }

    private void door(SceneContext ctx, int lx, int lz, int y0, BlockState door, Direction facing) {
        BlockState base = door.setValue(DoorBlock.FACING, facing);
        ctx.set(lx, y0 + 1, lz, base.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        ctx.set(lx, y0 + 2, lz, base.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    /** Registers seams, trap volumes and protection for the special cells around this chunk. */
    public void registerSpace(SceneContext ctx, long seed, int floorMin, int floorMax) {
        int margin = this.cell;
        int c0x = Math.floorDiv(ctx.originX() - margin, this.cell);
        int c0z = Math.floorDiv(ctx.originZ() - margin, this.cell);
        int c1x = Math.floorDiv(ctx.originX() + 15 + margin, this.cell);
        int c1z = Math.floorDiv(ctx.originZ() + 15 + margin, this.cell);
        int span = this.cell - 2;
        for (int floor = floorMin; floor <= Math.min(floorMax, this.specialFloor); floor++) {
            int y0 = floor * this.floorHeight;
            int top = y0 + this.floorHeight - 2;
            for (int cz = c0z; cz <= c1z; cz++) {
                for (int cx = c0x - RUN_CELLS; cx <= c1x; cx++) {
                    if (this.isRunStart(seed, floor, cx, cz) && cx + RUN_CELLS > c0x) {
                        int x0 = cx * this.cell;
                        int z0 = cz * this.cell;
                        ctx.seam(SeamVolume.translate(new Box(x0 + 2 * this.cell, y0 + 1, z0 + 5, x0 + 2 * this.cell + 1, y0 + 5, z0 + 8),
                                -this.cell, 0, 0, Direction.EAST));
                        ctx.protect(new Box(x0, y0, z0, x0 + RUN_CELLS * this.cell, top + 2, z0 + this.cell));
                    }
                    if (cx < c0x || !this.isClosed(seed, floor, cx, cz) || this.runStart(seed, floor, cx, cz) != Integer.MIN_VALUE) {
                        continue;
                    }
                    int x0 = cx * this.cell;
                    int z0 = cz * this.cell;
                    Box room = new Box(x0, y0, z0, x0 + this.cell + 1, top + 1, z0 + this.cell + 1);
                    ctx.trap(room);
                    ctx.protect(room);
                    ctx.seam(SeamVolume.translate(new Box(x0, y0 + 1, z0 + 1, x0 + 1, top, z0 + this.cell), span, 0, 0, Direction.WEST));
                    ctx.seam(SeamVolume.translate(new Box(x0 + this.cell, y0 + 1, z0 + 1, x0 + this.cell + 1, top, z0 + this.cell), -span, 0, 0, Direction.EAST));
                    ctx.seam(SeamVolume.translate(new Box(x0 + 1, y0 + 1, z0, x0 + this.cell, top, z0 + 1), 0, 0, span, Direction.NORTH));
                    ctx.seam(SeamVolume.translate(new Box(x0 + 1, y0 + 1, z0 + this.cell, x0 + this.cell, top, z0 + this.cell + 1), 0, 0, -span, Direction.SOUTH));
                    ctx.seam(SeamVolume.translate(new Box(x0 + 1, y0, z0 + 1, x0 + this.cell, y0 + 1, z0 + this.cell), 0, 3, 0, Direction.DOWN));
                    ctx.seam(SeamVolume.translate(new Box(x0 + 1, top - 1, z0 + 1, x0 + this.cell, top + 1, z0 + this.cell), 0, -4, 0, Direction.UP));
                }
            }
        }
    }

    public interface RoomSink {
        /** {@code (x0, y0, z0)} is the room's corner at floor level; its inside is {@code 1..cell-1} from there. */
        void accept(int floor, int cellX, int cellZ, int x0, int y0, int z0);
    }

    /** Calls back for the ordinary rooms of the decorated chunk picked by {@code chance} (with its own salt). */
    public void furnish(DecorationContext ctx, double chance, int salt, RoomSink sink) {
        long seed = ctx.seed();
        int floorMin = Math.floorDiv(ctx.info().minY(), this.floorHeight);
        int floorMax = Math.floorDiv(ctx.info().maxY(), this.floorHeight);
        int c0x = Math.floorDiv(ctx.originX(), this.cell);
        int c1x = Math.floorDiv(ctx.originX() + 15, this.cell);
        int c0z = Math.floorDiv(ctx.originZ(), this.cell);
        int c1z = Math.floorDiv(ctx.originZ() + 15, this.cell);
        for (int floor = floorMin; floor <= floorMax; floor++) {
            for (int cz = c0z; cz <= c1z; cz++) {
                for (int cx = c0x; cx <= c1x; cx++) {
                    if (Hash.chance(this.cellHash(seed, floor, cx, cz, salt), chance) && !this.special(seed, floor, cx, cz)
                            && !this.hole(seed, floor, cx, cz)) {
                        sink.accept(floor, cx, cz, cx * this.cell, floor * this.floorHeight, cz * this.cell);
                    }
                }
            }
        }
    }

    public interface ChestSink {
        void accept(int floor, int cellX, int cellZ, BlockPos pos, Direction facing);
    }

    /** Calls back for every chest position of the cells in the decorated chunk. */
    public void decorate(DecorationContext ctx, ChestSink sink) {
        long seed = ctx.seed();
        int floorMin = Math.floorDiv(ctx.info().minY(), this.floorHeight);
        int floorMax = Math.floorDiv(ctx.info().maxY(), this.floorHeight);
        int c0x = Math.floorDiv(ctx.originX(), this.cell);
        int c1x = Math.floorDiv(ctx.originX() + 15, this.cell);
        int c0z = Math.floorDiv(ctx.originZ(), this.cell);
        int c1z = Math.floorDiv(ctx.originZ() + 15, this.cell);
        for (int floor = floorMin; floor <= floorMax; floor++) {
            for (int cz = c0z; cz <= c1z; cz++) {
                for (int cx = c0x; cx <= c1x; cx++) {
                    if (!Hash.chance(this.cellHash(seed, floor, cx, cz, 0xC4E57), this.chestChance) || this.special(seed, floor, cx, cz)) {
                        continue;
                    }
                    BlockPos pos = new BlockPos(cx * this.cell + this.cell - 2, floor * this.floorHeight + 1, cz * this.cell + this.cell - 2);
                    if (ctx.canPlace(pos) && ctx.getBlock(pos).isAir()) {
                        sink.accept(floor, cx, cz, pos, Direction.NORTH);
                    }
                }
            }
        }
    }
}
