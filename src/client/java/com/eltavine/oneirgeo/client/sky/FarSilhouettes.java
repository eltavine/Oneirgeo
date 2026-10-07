package com.eltavine.oneirgeo.client.sky;

import com.eltavine.oneirgeo.client.config.OneirgeoConfig;
import com.eltavine.oneirgeo.network.LandmarksPayload;
import com.eltavine.oneirgeo.world.gen.landmark.Landmark;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

/**
 * Megastructures beyond the render distance, drawn as hazy low-poly silhouettes. Anything farther
 * than the far plane is pulled in along its line of sight and shrunk by the same factor, so it keeps
 * its apparent size; terrain drawn closer still covers it through the depth test.
 */
public final class FarSilhouettes {
    private static final int SEGMENTS = 14;
    private static List<Landmark> landmarks = List.of();

    private FarSilhouettes() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(LandmarksPayload.TYPE, (payload, context) -> landmarks = payload.landmarks());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> landmarks = List.of());
        LevelRenderEvents.COLLECT_SUBMITS.register(FarSilhouettes::submit);
    }

    public static List<Landmark> current() {
        return landmarks;
    }

    private static void submit(LevelRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        List<Landmark> giants = DistantGiants.current();
        if (level == null || (landmarks.isEmpty() && giants.isEmpty()) || !OneirgeoConfig.get().farSilhouettes) {
            return;
        }
        Vec3 eye = context.levelState().cameraRenderState.pos;
        double near = client.options.getEffectiveRenderDistance() * 16.0 - 24.0;
        double far = Math.max(client.options.getEffectiveRenderDistance() * 64.0, 256.0) * 0.8;
        Vector3fc fog = level.environmentAttributes().getValue(EnvironmentAttributes.FOG_COLOR, eye);
        PoseStack poseStack = context.poseStack();
        List<Landmark> shown = giants.isEmpty() ? landmarks : concat(landmarks, giants);
        context.submitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, consumer) -> {
            for (Landmark landmark : shown) {
                double dx = landmark.x() - eye.x;
                double dz = landmark.z() - eye.z;
                double distance = Math.sqrt(dx * dx + dz * dz) - landmark.horizontalReach();
                if (distance < near) {
                    continue;
                }
                double scale = distance > far ? far / distance : 1.0;
                float haze = (float) Math.min(0.9, 1.0 - Math.exp(-(distance - near) / 2400.0));
                Shape shape = new Shape(consumer, pose, landmark, eye, scale, fog, haze);
                shape.emit();
            }
        });
    }

    private static List<Landmark> concat(List<Landmark> a, List<Landmark> b) {
        List<Landmark> out = new java.util.ArrayList<>(a.size() + b.size());
        out.addAll(a);
        out.addAll(b);
        return out;
    }

    /** Emits the quads of one landmark, already shrunk and hazed. */
    private record Shape(VertexConsumer consumer, PoseStack.Pose pose, Landmark l, Vec3 eye, double scale, Vector3fc fog, float haze) {
        void emit() {
            switch (this.l.shape()) {
                case BOX -> this.box();
                case CYLINDER -> this.prism(this.l.radiusX(), this.l.radiusX());
                case SPIRE -> this.prism(this.l.radiusX(), 0);
                case STALACTITE -> this.prism(0, this.l.radiusX());
                case SPHERE -> this.sphere();
                case RING -> this.ring();
            }
        }

        private int colour(float light) {
            int c = this.l.color();
            float r = ((c >> 16) & 0xFF) / 255.0F * light;
            float g = ((c >> 8) & 0xFF) / 255.0F * light;
            float b = (c & 0xFF) / 255.0F * light;
            r += (this.fog.x() - r) * this.haze;
            g += (this.fog.y() - g) * this.haze;
            b += (this.fog.z() - b) * this.haze;
            int a = (c >>> 24) == 0 ? 255 : (c >>> 24);
            return a << 24 | clamp(r) << 16 | clamp(g) << 8 | clamp(b);
        }

        private static int clamp(float v) {
            return Math.max(0, Math.min(255, Math.round(v * 255.0F)));
        }

        private void vertex(double x, double y, double z, int argb) {
            float px = (float) ((x - this.eye.x) * this.scale);
            float py = (float) ((y - this.eye.y) * this.scale);
            float pz = (float) ((z - this.eye.z) * this.scale);
            this.consumer.addVertex(this.pose, px, py, pz).setColor(argb);
        }

        private void quad(double[] a, double[] b, double[] c, double[] d, int argb) {
            this.vertex(a[0], a[1], a[2], argb);
            this.vertex(b[0], b[1], b[2], argb);
            this.vertex(c[0], c[1], c[2], argb);
            this.vertex(d[0], d[1], d[2], argb);
        }

        private void box() {
            double x0 = this.l.x() - this.l.radiusX();
            double x1 = this.l.x() + this.l.radiusX();
            double z0 = this.l.z() - this.l.radiusZ();
            double z1 = this.l.z() + this.l.radiusZ();
            double y0 = this.l.y();
            double y1 = this.l.maxY() + 1;
            this.quad(new double[]{x0, y1, z0}, new double[]{x0, y1, z1}, new double[]{x1, y1, z1}, new double[]{x1, y1, z0}, this.colour(1.0F));
            this.quad(new double[]{x0, y0, z0}, new double[]{x1, y0, z0}, new double[]{x1, y1, z0}, new double[]{x0, y1, z0}, this.colour(0.8F));
            this.quad(new double[]{x0, y0, z1}, new double[]{x0, y1, z1}, new double[]{x1, y1, z1}, new double[]{x1, y0, z1}, this.colour(0.8F));
            this.quad(new double[]{x0, y0, z0}, new double[]{x0, y1, z0}, new double[]{x0, y1, z1}, new double[]{x0, y0, z1}, this.colour(0.65F));
            this.quad(new double[]{x1, y0, z0}, new double[]{x1, y0, z1}, new double[]{x1, y1, z1}, new double[]{x1, y1, z0}, this.colour(0.65F));
        }

        /** A cone frustum: radius at the bottom and at the top; zero makes a point. */
        private void prism(double bottom, double top) {
            double y0 = this.l.y();
            double y1 = this.l.maxY() + 1;
            for (int i = 0; i < SEGMENTS; i++) {
                double a0 = Math.PI * 2 * i / SEGMENTS;
                double a1 = Math.PI * 2 * (i + 1) / SEGMENTS;
                float light = 0.62F + 0.38F * (float) (0.5 + 0.5 * Math.cos((a0 + a1) * 0.5 - 0.8));
                int argb = this.colour(light);
                double[] b0 = {this.l.x() + Math.cos(a0) * bottom, y0, this.l.z() + Math.sin(a0) * bottom};
                double[] b1 = {this.l.x() + Math.cos(a1) * bottom, y0, this.l.z() + Math.sin(a1) * bottom};
                double[] t1 = {this.l.x() + Math.cos(a1) * top, y1, this.l.z() + Math.sin(a1) * top};
                double[] t0 = {this.l.x() + Math.cos(a0) * top, y1, this.l.z() + Math.sin(a0) * top};
                this.quad(b0, b1, t1, t0, argb);
                if (top > 0) {
                    double[] c = {this.l.x(), y1, this.l.z()};
                    this.quad(c, t0, t1, c, this.colour(1.0F));
                }
            }
        }

        private void sphere() {
            int rings = 8;
            double cy = this.l.y() + this.l.height() * 0.5;
            double ry = this.l.height() * 0.5;
            for (int j = 0; j < rings; j++) {
                double p0 = Math.PI * j / rings - Math.PI / 2;
                double p1 = Math.PI * (j + 1) / rings - Math.PI / 2;
                for (int i = 0; i < SEGMENTS; i++) {
                    double a0 = Math.PI * 2 * i / SEGMENTS;
                    double a1 = Math.PI * 2 * (i + 1) / SEGMENTS;
                    float light = 0.55F + 0.45F * (float) ((Math.sin(p0) + 1.0) * 0.5);
                    this.quad(this.onSphere(a0, p0, cy, ry), this.onSphere(a1, p0, cy, ry), this.onSphere(a1, p1, cy, ry), this.onSphere(a0, p1, cy, ry), this.colour(light));
                }
            }
        }

        private double[] onSphere(double azimuth, double pitch, double cy, double ry) {
            return new double[]{this.l.x() + Math.cos(azimuth) * Math.cos(pitch) * this.l.radiusX(), cy + Math.sin(pitch) * ry,
                    this.l.z() + Math.sin(azimuth) * Math.cos(pitch) * this.l.radiusZ()};
        }

        private void ring() {
            int around = 32;
            double cy = this.l.y() + this.l.height() * 0.5;
            double tube = this.l.radiusZ();
            for (int i = 0; i < around; i++) {
                double a0 = Math.PI * 2 * i / around;
                double a1 = Math.PI * 2 * (i + 1) / around;
                for (int k = 0; k < 4; k++) {
                    double t0 = Math.PI * 2 * k / 4;
                    double t1 = Math.PI * 2 * (k + 1) / 4;
                    int argb = this.colour(k == 1 ? 1.0F : 0.75F);
                    this.quad(this.onRing(a0, t0, cy, tube), this.onRing(a1, t0, cy, tube), this.onRing(a1, t1, cy, tube), this.onRing(a0, t1, cy, tube), argb);
                }
            }
        }

        private double[] onRing(double around, double tubeAngle, double cy, double tube) {
            double radial = this.l.radiusX() + Math.cos(tubeAngle) * tube;
            return new double[]{this.l.x() + Math.cos(around) * radial, cy + Math.sin(tubeAngle) * tube, this.l.z() + Math.sin(around) * radial};
        }
    }
}
