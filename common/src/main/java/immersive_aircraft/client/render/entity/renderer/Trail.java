package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;

public class Trail {
    private static final Identifier identifier = Main.locate("textures/entity/trail.png");

    private final float[] buffer;
    private final int size;
    private int lastIndex;
    private int entries;
    private int nullEntries;

    public Trail(int length) {
        buffer = new float[7 * length];
        size = length;
    }

    public void add(Vector4f first, Vector4f second, float alpha) {
        if (alpha <= 0.0) {
            nullEntries++;
        } else {
            nullEntries = 0;
        }

        if (nullEntries < size) {
            int i = lastIndex * 7;
            buffer[i] = first.getX();
            buffer[i + 1] = first.getY();
            buffer[i + 2] = first.getZ();
            buffer[i + 3] = second.getX();
            buffer[i + 4] = second.getY();
            buffer[i + 5] = second.getZ();
            buffer[i + 6] = alpha;
        }

        lastIndex = (lastIndex + 1) % size;
        entries++;
    }

    public void render(VertexConsumerProvider vertexConsumerProvider, MatrixStack.Entry matrices) {
        if (nullEntries >= size) {
            return;
        }

        VertexConsumer lineVertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(identifier));
        int light = 15728640;

        Vec3d pos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

        //todo a custom vertex indexing methode would be beneficial here
        for (int i = 1; i < Math.min(entries, size); i++) {
            int pre = ((i + lastIndex - 1) % size) * 7;
            int index = ((i + lastIndex) % size) * 7;

            int a;
            Vec3f v;

            a = (int)((1.0f - ((float)i) / size * 255) * buffer[pre + 6]);

            v = new Vec3f((float)(buffer[pre] - pos.x), (float)(buffer[pre + 1] - pos.y), (float)(buffer[pre + 2] - pos.z));
            v.transform(matrices.getNormalMatrix());
            lineVertexConsumer.vertex(v.getX(), v.getY(), v.getZ(), 1, 1, 1, a, 0, 0, OverlayTexture.DEFAULT_UV, light, 1, 0, 0);

            v = new Vec3f((float)(buffer[pre + 3] - pos.x), (float)(buffer[pre + 4] - pos.y), (float)(buffer[pre + 5] - pos.z));
            v.transform(matrices.getNormalMatrix());
            lineVertexConsumer.vertex(v.getX(), v.getY(), v.getZ(), 1, 1, 1, a, 0, 1, OverlayTexture.DEFAULT_UV, light, 1, 0, 0);

            a = i == (size - 1) ? 0 : (int)((1.0f - ((float)i + 1) / size * 255) * buffer[index + 6]);

            v = new Vec3f((float)(buffer[index + 3] - pos.x), (float)(buffer[index + 4] - pos.y), (float)(buffer[index + 5] - pos.z));
            v.transform(matrices.getNormalMatrix());
            lineVertexConsumer.vertex(v.getX(), v.getY(), v.getZ(), 1, 1, 1, a, 1, 1, OverlayTexture.DEFAULT_UV, light, 1, 0, 0);

            v = new Vec3f((float)(buffer[index] - pos.x), (float)(buffer[index + 1] - pos.y), (float)(buffer[index + 2] - pos.z));
            v.transform(matrices.getNormalMatrix());
            lineVertexConsumer.vertex(v.getX(), v.getY(), v.getZ(), 1, 1, 1, a, 1, 0, OverlayTexture.DEFAULT_UV, light, 1, 0, 0);
        }
    }
}
