package com.eltavine.oneirgeo.world.gen.landmark;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A megastructure that crosses layers: monoliths, sky pillars, hanging spires, halos.
 * The same description drives block placement on the server and far silhouettes on the client.
 *
 * @param x       centre X
 * @param y       bottom Y
 * @param z       centre Z
 * @param radiusX half width along X (or radius for round shapes)
 * @param height  total height
 * @param radiusZ half width along Z
 * @param color   ARGB colour of the far silhouette
 * @param style   field-specific material and decoration style
 */
public record Landmark(Shape shape, int x, int y, int z, int radiusX, int height, int radiusZ, int color, int style) {
    public static final StreamCodec<ByteBuf, Landmark> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Landmark decode(ByteBuf buf) {
            Shape shape = Shape.values()[buf.readByte()];
            return new Landmark(shape,
                    ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                    buf.readInt(), buf.readByte());
        }

        @Override
        public void encode(ByteBuf buf, Landmark l) {
            buf.writeByte(l.shape.ordinal());
            ByteBufCodecs.VAR_INT.encode(buf, l.x);
            ByteBufCodecs.VAR_INT.encode(buf, l.y);
            ByteBufCodecs.VAR_INT.encode(buf, l.z);
            ByteBufCodecs.VAR_INT.encode(buf, l.radiusX);
            ByteBufCodecs.VAR_INT.encode(buf, l.height);
            ByteBufCodecs.VAR_INT.encode(buf, l.radiusZ);
            buf.writeInt(l.color);
            buf.writeByte(l.style);
        }
    };

    public enum Shape {
        /** Upright box: monoliths and slabs. */
        BOX,
        /** Upright cylinder: pillars and chimneys. */
        CYLINDER,
        /** Cone with its apex at the top. */
        SPIRE,
        /** Cone hanging from above with its apex at the bottom. */
        STALACTITE,
        /** Ellipsoid filling the bounds. */
        SPHERE,
        /** Flat horizontal ring; radiusZ is the tube thickness. */
        RING
    }

    /** Landmarks with a fully transparent colour shape terrain but are never drawn from afar. */
    public boolean visible() {
        return (this.color >>> 24) != 0;
    }

    public int maxY() {
        return this.y + this.height - 1;
    }

    public int horizontalReach() {
        return this.shape == Shape.RING ? this.radiusX + this.radiusZ : Math.max(this.radiusX, this.radiusZ);
    }

    public boolean intersects(int minX, int minZ, int maxX, int maxZ) {
        int reach = this.horizontalReach() + 4;
        return this.x + reach >= minX && this.x - reach <= maxX && this.z + reach >= minZ && this.z - reach <= maxZ;
    }

    /**
     * Signed coverage test: returns true when the block centre lies inside the solid shape.
     */
    public boolean contains(int bx, int by, int bz) {
        if (by < this.y || by > this.maxY()) {
            return false;
        }
        double dx = bx + 0.5 - this.x;
        double dz = bz + 0.5 - this.z;
        double t = (by - this.y + 0.5) / this.height;
        return switch (this.shape) {
            case BOX -> Math.abs(dx) <= this.radiusX && Math.abs(dz) <= this.radiusZ;
            case CYLINDER -> dx * dx + dz * dz <= (double) this.radiusX * this.radiusX;
            case SPIRE -> {
                double r = this.radiusX * (1.0 - t);
                yield dx * dx + dz * dz <= r * r;
            }
            case STALACTITE -> {
                double r = this.radiusX * t;
                yield dx * dx + dz * dz <= r * r;
            }
            case SPHERE -> {
                double ny = (t - 0.5) * 2.0;
                double nx = dx / Math.max(1, this.radiusX);
                double nz = dz / Math.max(1, this.radiusZ);
                yield nx * nx + ny * ny + nz * nz <= 1.0;
            }
            case RING -> {
                double radial = Math.sqrt(dx * dx + dz * dz) - this.radiusX;
                double vertical = (t - 0.5) * this.height;
                yield radial * radial + vertical * vertical <= (double) this.radiusZ * this.radiusZ;
            }
        };
    }
}
