package com.eltavine.oneirgeo.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.stream.IntStream;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import net.minecraft.world.phys.AABB;

/** Axis aligned box in block space; min is inclusive and max is exclusive. */
public record Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    public static final Codec<Box> CODEC = Codec.INT_STREAM.comapFlatMap(
            stream -> Util.fixedSize(stream, 6).map(a -> new Box(a[0], a[1], a[2], a[3], a[4], a[5])),
            box -> IntStream.of(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)
    );

    public static final StreamCodec<ByteBuf, Box> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Box decode(ByteBuf buf) {
            return new Box(
                    ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf));
        }

        @Override
        public void encode(ByteBuf buf, Box box) {
            ByteBufCodecs.VAR_INT.encode(buf, box.minX);
            ByteBufCodecs.VAR_INT.encode(buf, box.minY);
            ByteBufCodecs.VAR_INT.encode(buf, box.minZ);
            ByteBufCodecs.VAR_INT.encode(buf, box.maxX);
            ByteBufCodecs.VAR_INT.encode(buf, box.maxY);
            ByteBufCodecs.VAR_INT.encode(buf, box.maxZ);
        }
    };

    public static Box ofSize(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
        return new Box(x, y, z, x + sizeX, y + sizeY, z + sizeZ);
    }

    public boolean contains(double x, double y, double z) {
        return x >= this.minX && x < this.maxX && y >= this.minY && y < this.maxY && z >= this.minZ && z < this.maxZ;
    }

    public boolean containsBlock(int x, int y, int z) {
        return x >= this.minX && x < this.maxX && y >= this.minY && y < this.maxY && z >= this.minZ && z < this.maxZ;
    }

    public boolean intersectsColumnRange(int minX, int minZ, int maxX, int maxZ) {
        return this.maxX > minX && this.minX < maxX && this.maxZ > minZ && this.minZ < maxZ;
    }

    public Box offset(int dx, int dy, int dz) {
        return new Box(this.minX + dx, this.minY + dy, this.minZ + dz, this.maxX + dx, this.maxY + dy, this.maxZ + dz);
    }

    public Box inflate(int amount) {
        return new Box(this.minX - amount, this.minY - amount, this.minZ - amount, this.maxX + amount, this.maxY + amount, this.maxZ + amount);
    }

    public double centerX() {
        return (this.minX + this.maxX) * 0.5;
    }

    public double centerY() {
        return (this.minY + this.maxY) * 0.5;
    }

    public double centerZ() {
        return (this.minZ + this.maxZ) * 0.5;
    }

    public AABB toAabb() {
        return new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    static DataResult<Box> validate(Box box) {
        return box.maxX > box.minX && box.maxY > box.minY && box.maxZ > box.minZ
                ? DataResult.success(box)
                : DataResult.error(() -> "Empty box " + box);
    }
}
