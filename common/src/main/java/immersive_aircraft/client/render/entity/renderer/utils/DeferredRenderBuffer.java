package immersive_aircraft.client.render.entity.renderer.utils;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeferredRenderBuffer implements MultiBufferSource {
    private final Map<RenderType, RecordingVertexConsumer> buffers = new LinkedHashMap<>();

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return buffers.computeIfAbsent(renderType, ignored -> new RecordingVertexConsumer());
    }

    public void submit(SubmitNodeCollector collector) {
        buffers.forEach((renderType, buffer) -> {
            if (buffer.vertices.isEmpty()) {
                return;
            }
            List<Vertex> vertices = List.copyOf(buffer.vertices);
            collector.submitCustomGeometry(new com.mojang.blaze3d.vertex.PoseStack(), renderType, (pose, output) -> vertices.forEach(vertex -> vertex.write(output)));
        });
    }

    private static class RecordingVertexConsumer implements VertexConsumer {
        private final List<Vertex> vertices = new ArrayList<>();
        private Vertex current;

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            current = new Vertex(x, y, z);
            vertices.add(current);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            if (current != null) {
                current.red = red;
                current.green = green;
                current.blue = blue;
                current.alpha = alpha;
            }
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            return setColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >>> 24) & 0xFF);
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            if (current != null) {
                current.u = u;
                current.v = v;
            }
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            if (current != null) {
                current.overlay = u | (v << 16);
            }
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            if (current != null) {
                current.light = u | (v << 16);
            }
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            if (current != null) {
                current.normalX = x;
                current.normalY = y;
                current.normalZ = z;
            }
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }
    }

    private static class Vertex {
        private final float x;
        private final float y;
        private final float z;
        private int red = 255;
        private int green = 255;
        private int blue = 255;
        private int alpha = 255;
        private float u;
        private float v;
        private int overlay;
        private int light;
        private float normalX;
        private float normalY = 1.0f;
        private float normalZ;

        private Vertex(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private void write(VertexConsumer output) {
            output.addVertex(x, y, z)
                    .setColor(red, green, blue, alpha)
                    .setUv(u, v)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(normalX, normalY, normalZ);
        }
    }
}
