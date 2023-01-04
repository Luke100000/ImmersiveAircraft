package immersive_aircraft.entity.misc;

import immersive_aircraft.Config;
import immersive_aircraft.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Trail {
    private static final Identifier identifier = Main.locate("textures/entity/trail.png");

    private final float[] buffer;
    private final int size;
    private final float gray;
    private int lastIndex;
    private int entries;
    private int nullEntries;

    public Trail(int length) {
        this(length, 1.0f);
    }

    public Trail(int length, float gray) {
        buffer = new float[7 * length];
        size = length;
        this.gray = gray;
    }

    public void add(Vector4f first, Vector4f second, float alpha) {
        if (!Config.getInstance().enableTrails) {
            return;
        }

        if (alpha <= 0.0) {
            nullEntries++;
        } else {
            nullEntries = 0;
        }

        if (nullEntries < size) {
            int i = lastIndex * 7;
            buffer[i] = first.x;
            buffer[i + 1] = first.y;
            buffer[i + 2] = first.z;
            buffer[i + 3] = second.x;
            buffer[i + 4] = second.y;
            buffer[i + 5] = second.z;
            buffer[i + 6] = alpha;
        }

        lastIndex = (lastIndex + 1) % size;
        entries++;
    }

    private void vertex(VertexConsumer lineVertexConsumer, Matrix3f matrix, float u, float v, int index, Vec3d pos, float a, int light) {
        Vector3f p = new Vector3f((float)(buffer[index] - pos.x), (float)(buffer[index + 1] - pos.y), (float)(buffer[index + 2] - pos.z));
        matrix.transform(p);
        lineVertexConsumer.vertex(p.x, p.y, p.z, gray, gray, gray, a, u, v, OverlayTexture.DEFAULT_UV, light, 1, 0, 0);
    }

    public void render(VertexConsumerProvider vertexConsumerProvider, MatrixStack.Entry matrices) {
        if (nullEntries >= size || entries == 0) {
            return;
        }

        VertexConsumer lineVertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getBeaconBeam(identifier, true));
        int light = 15728640;

        Vec3d pos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        Matrix3f matrix = matrices.getNormalMatrix();

        //todo a custom vertex indexing methode would be beneficial here
        for (int i = 1; i < Math.min(entries, size); i++) {
            int pre = ((i + lastIndex - 1) % size) * 7;
            int index = ((i + lastIndex) % size) * 7;

            int a1 = (int)((1.0f - ((float)i) / size * 255) * buffer[pre + 6]);
            int a2 = i == (size - 1) ? 0 : (int)((1.0f - ((float)i + 1) / size * 255) * buffer[index + 6]);

            vertex(lineVertexConsumer, matrix, 0, 0, pre, pos, a1, light);
            vertex(lineVertexConsumer, matrix, 0, 1, pre + 3, pos, a1, light);
            vertex(lineVertexConsumer, matrix, 1, 1, index + 3, pos, a2, light);
            vertex(lineVertexConsumer, matrix, 1, 0, index, pos, a2, light);

            //todo the anti culling here is stupid
            vertex(lineVertexConsumer, matrix, 1, 0, index, pos, a2, light);
            vertex(lineVertexConsumer, matrix, 1, 1, index + 3, pos, a2, light);
            vertex(lineVertexConsumer, matrix, 0, 1, pre + 3, pos, a1, light);
            vertex(lineVertexConsumer, matrix, 0, 0, pre, pos, a1, light);
        }
    }
}
