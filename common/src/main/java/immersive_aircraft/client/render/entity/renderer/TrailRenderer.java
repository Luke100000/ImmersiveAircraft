package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import immersive_aircraft.Main;
import immersive_aircraft.entity.misc.Trail;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class TrailRenderer {
    private static final ResourceLocation identifier = Main.locate("textures/entity/trail.png");

    public static void render(Trail trail, MultiBufferSource vertexConsumerProvider, PoseStack.Pose matrices) {
        if (trail.nullEntries >= trail.size || trail.entries == 0) {
            return;
        }

        VertexConsumer lineVertexConsumer = vertexConsumerProvider.getBuffer(RenderType.beaconBeam(identifier, true));
        int light = 15728640;

        Vec3 pos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Matrix3f matrix = matrices.normal();

        //todo a custom vertex indexing methode would be beneficial here
        for (int i = 1; i < Math.min(trail.entries, trail.size); i++) {
            int pre = ((i + trail.lastIndex - 1) % trail.size) * 7;
            int index = ((i + trail.lastIndex) % trail.size) * 7;

            int a1 = (int) ((1.0f - ((float) i) / trail.size * 255) * trail.buffer[pre + 6]);
            int a2 = i == (trail.size - 1) ? 0 : (int) ((1.0f - ((float) i + 1) / trail.size * 255) * trail.buffer[index + 6]);

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

    private static void vertex(Trail trail, VertexConsumer lineVertexConsumer, Matrix3f matrix, float u, float v, int index, Vec3 pos, float a, int light) {
        Vector3f p = new Vector3f((float) (trail.buffer[index] - pos.x), (float) (trail.buffer[index + 1] - pos.y), (float) (trail.buffer[index + 2] - pos.z));
        matrix.transform(p);
        lineVertexConsumer.vertex(p.x, p.y, p.z, trail.gray, trail.gray, trail.gray, a, u, v, OverlayTexture.NO_OVERLAY, light, 1, 0, 0);
    }
}
