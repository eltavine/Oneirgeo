package com.eltavine.oneirgeo.space;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

/**
 * A trigger box that moves whatever enters it by a fixed transform. Both sides of a seam are generated
 * identically, so the jump is invisible: corridors that never end, rooms bigger than their walls, loops.
 *
 * @param rotation quarter turns clockwise around the trigger centre, applied before the offset
 * @param direction {@link Direction#get3DDataValue()} the entity must be moving along, or -1 for any
 */
public record SeamVolume(Box trigger, int dx, int dy, int dz, int rotation, int direction) {
    /** Spare bit of {@link #rotation} (only its low two bits turn): the seam lets through whoever remembers the whole story. */
    public static final int AWAKE_PASS = 4;

    public static final Codec<SeamVolume> CODEC = RecordCodecBuilder.create(i -> i.group(
            Box.CODEC.fieldOf("trigger").forGetter(SeamVolume::trigger),
            Codec.INT.fieldOf("dx").forGetter(SeamVolume::dx),
            Codec.INT.fieldOf("dy").forGetter(SeamVolume::dy),
            Codec.INT.fieldOf("dz").forGetter(SeamVolume::dz),
            Codec.INT.optionalFieldOf("rotation", 0).forGetter(SeamVolume::rotation),
            Codec.INT.optionalFieldOf("direction", -1).forGetter(SeamVolume::direction)
    ).apply(i, SeamVolume::new));

    public static final StreamCodec<ByteBuf, SeamVolume> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SeamVolume decode(ByteBuf buf) {
            Box box = Box.STREAM_CODEC.decode(buf);
            int dx = ByteBufCodecs.VAR_INT.decode(buf);
            int dy = ByteBufCodecs.VAR_INT.decode(buf);
            int dz = ByteBufCodecs.VAR_INT.decode(buf);
            int rotation = buf.readByte();
            int direction = buf.readByte();
            return new SeamVolume(box, dx, dy, dz, rotation, direction);
        }

        @Override
        public void encode(ByteBuf buf, SeamVolume seam) {
            Box.STREAM_CODEC.encode(buf, seam.trigger);
            ByteBufCodecs.VAR_INT.encode(buf, seam.dx);
            ByteBufCodecs.VAR_INT.encode(buf, seam.dy);
            ByteBufCodecs.VAR_INT.encode(buf, seam.dz);
            buf.writeByte(seam.rotation);
            buf.writeByte(seam.direction);
        }
    };

    public static SeamVolume translate(Box trigger, int dx, int dy, int dz, Direction moving) {
        return new SeamVolume(trigger, dx, dy, dz, 0, moving == null ? -1 : moving.get3DDataValue());
    }

    public boolean letsAwakePass() {
        return (this.rotation & AWAKE_PASS) != 0;
    }

    public SeamVolume lettingAwakePass() {
        return new SeamVolume(this.trigger, this.dx, this.dy, this.dz, this.rotation | AWAKE_PASS, this.direction);
    }

    public boolean accepts(Vec3 position, Vec3 motion) {
        if (!this.trigger.contains(position.x, position.y, position.z)) {
            return false;
        }
        if (this.direction < 0) {
            return true;
        }
        Direction dir = Direction.from3DDataValue(this.direction);
        double along = motion.x * dir.getStepX() + motion.y * dir.getStepY() + motion.z * dir.getStepZ();
        return along > 1.0E-4;
    }

    public Vec3 apply(Vec3 position) {
        double cx = this.trigger.centerX();
        double cz = this.trigger.centerZ();
        double rx = position.x - cx;
        double rz = position.z - cz;
        for (int i = 0; i < (this.rotation & 3); i++) {
            double t = rx;
            rx = -rz;
            rz = t;
        }
        return new Vec3(cx + rx + this.dx, position.y + this.dy, cz + rz + this.dz);
    }

    public Vec3 rotateVector(Vec3 v) {
        double x = v.x;
        double z = v.z;
        for (int i = 0; i < (this.rotation & 3); i++) {
            double t = x;
            x = -z;
            z = t;
        }
        return new Vec3(x, v.y, z);
    }

    public float rotateYaw(float yaw) {
        return yaw + 90.0F * (this.rotation & 3);
    }
}
