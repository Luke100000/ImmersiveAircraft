package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.misc.Trail;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public class TrailRenderer {
    private static final Identifier identifier = Main.locate("textures/entity/trail.png");

    public static void render(Trail trail, VertexConsumerProvider vertexConsumerProvider, MatrixStack.Entry matrices) {
        if (trail.nullEntries >= trail.size || trail.entries == 0) {
            return;
        }

        VertexConsumer lineVertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getBeaconBeam(identifier, true));
        int light = 15728640;

        Vec3d pos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        Matrix3f matrix = matrices.getNormal();

        //todo a custom vertex indexing methode would be beneficial here
        for (int i = 1; i < Math.min(trail.entries, trail.size); i++) {
            int pre = ((i + trail.lastIndex - 1) % trail.size) * 7;
            int index = ((i + trail.lastIndex) % trail.size) * 7;

            int a1 = (int)((1.0f - ((float)i) / trail.size * 255) * trail.buffer[pre + 6]);
            int a2 = i == (trail.size - 1) ? 0 : (int)((1.0f - ((float)i + 1) / trail.size * 255) * trail.buffer[index + 6]);

            vertex(trail, lineVertexConsumer, matrix, 0, 0, pre, pos, a1, light);
            vertex(trail, lineVertexConsumer, matrix, 0, 1, pre + 3, pos, a1, light);
            vertex(trail, lineVertexConsumer, matrix, 1, 1, index + 3, pos, a2, light);
            vertex(trail, lineVertexConsumer, matrix, 1, 0, index, pos, a2, light);

            //todo the anti culling here is stupid
            vertex(trail, lineVertexConsumer, matrix, 1, 0, index, pos, a2, light);
            vertex(trail, lineVertexConsumer, matrix, 1, 1, index + 3, pos, a2, light);
            vertex(trail, lineVertexConsumer, matrix, 0, 1, pre + 3, pos, a1, light);
            vertex(trail, lineVertexConsumer, matrix, 0, 0, pre, pos, a1, light);
        }
    }

    private static void vertex(Trail trail, VertexConsumer lineVertexConsumer, Matrix3f matrix, float u, float v, int index, Vec3d pos, float a, int light) {
        Vec3f p = new Vec3f((float)(trail.buffer[index] - pos.x), (float)(trail.buffer[index + 1] - pos.y), (float)(trail.buffer[index + 2] - pos.z));
        p.transform(matrix);
        lineVertexConsumer.vertex(p.getX(), p.getY(), p.getZ(), trail.gray, trail.gray, trail.gray, a, u, v, OverlayTexture.DEFAULT_UV, light, 1, 0, 0);
    }
}
