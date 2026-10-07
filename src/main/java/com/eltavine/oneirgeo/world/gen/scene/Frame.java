package com.eltavine.oneirgeo.world.gen.scene;

import net.minecraft.core.Direction;

/**
 * A local building frame: origin plus a facing. Local +b points to the front (the door side) and
 * local +a points to the right of someone standing outside looking in, as when facing south.
 */
public record Frame(int x, int y, int z, Direction front) {
    private int turns() {
        return switch (this.front) {
            case WEST -> 1;
            case NORTH -> 2;
            case EAST -> 3;
            default -> 0;
        };
    }

    public int worldX(int a, int b) {
        return switch (this.turns()) {
            case 1 -> this.x - b;
            case 2 -> this.x - a;
            case 3 -> this.x + b;
            default -> this.x + a;
        };
    }

    public int worldZ(int a, int b) {
        return switch (this.turns()) {
            case 1 -> this.z + a;
            case 2 -> this.z - b;
            case 3 -> this.z - a;
            default -> this.z + b;
        };
    }

    /** Rotates a direction given in the south-facing frame into world space. */
    public Direction world(Direction local) {
        if (local.getAxis() == Direction.Axis.Y) {
            return local;
        }
        Direction d = local;
        for (int i = 0; i < this.turns(); i++) {
            d = d.getClockWise();
        }
        return d;
    }
}
