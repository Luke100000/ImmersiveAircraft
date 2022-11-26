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

    public Trail(int length) {
        buffer = new float[6 * 2 * length];
        size = length;
    }

    public void add(Vector4f first, Vector4f second) {
        buffer[lastIndex * 6] = first.getX();
        buffer[lastIndex * 6 + 1] = first.getY();
        buffer[lastIndex * 6 + 2] = first.getZ();
        buffer[lastIndex * 6 + 3] = second.getX();
        buffer[lastIndex * 6 + 4] = second.getY();
        buffer[lastIndex * 6 + 5] = second.getZ();

        lastIndex = (lastIndex + 1) % size;
        entries++;
    }

    public void render(VertexConsumerProvider vertexConsumerProvider, MatrixStack.Entry matrices) {
        VertexConsumer lineVertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(identifier));
        int light = 15728640;

        Vec3d pos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

        //todo a custom vertex indexing methode would be beneficial here
        for (int i = 1; i < Math.min(entries, size); i++) {
            int pre = ((i + lastIndex - 1) % size) * 6;
            int index = ((i + lastIndex) % size) * 6;

            int a;
            Vec3f v;

            a = (int)(1.0f - ((float)i) / size * 255);

            v = new Vec3f((float)(buffer[pre] - pos.x), (float)(buffer[pre + 1] - pos.y), (float)(buffer[pre + 2] - pos.z));
            v.transform(matrices.getNormalMatrix());
            lineVertexConsumer.vertex(v.getX(), v.getY(), v.getZ(), 1, 1, 1, a, 0, 0, OverlayTexture.DEFAULT_UV, light, 1, 0, 0);

            v = new Vec3f((float)(buffer[pre + 3] - pos.x), (float)(buffer[pre + 4] - pos.y), (float)(buffer[pre + 5] - pos.z));
            v.transform(matrices.getNormalMatrix());
            lineVertexConsumer.vertex(v.getX(), v.getY(), v.getZ(), 1, 1, 1, a, 0, 1, OverlayTexture.DEFAULT_UV, light, 1, 0, 0);

            a = i == (size-1) ? 0 : (int)(1.0f - ((float)i + 1) / size * 255);

            v = new Vec3f((float)(buffer[index + 3] - pos.x), (float)(buffer[index + 4] - pos.y), (float)(buffer[index + 5] - pos.z));
            v.transform(matrices.getNormalMatrix());
            lineVertexConsumer.vertex(v.getX(), v.getY(), v.getZ(), 1, 1, 1, a, 1, 1, OverlayTexture.DEFAULT_UV, light, 1, 0, 0);

            v = new Vec3f((float)(buffer[index] - pos.x), (float)(buffer[index + 1] - pos.y), (float)(buffer[index + 2] - pos.z));
            v.transform(matrices.getNormalMatrix());
            lineVertexConsumer.vertex(v.getX(), v.getY(), v.getZ(), 1, 1, 1, a, 1, 0, OverlayTexture.DEFAULT_UV, light, 1, 0, 0);
        }
    }
}
