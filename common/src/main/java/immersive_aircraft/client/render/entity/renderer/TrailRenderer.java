package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import immersive_aircraft.Main;
import immersive_aircraft.entity.misc.Trail;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class TrailRenderer {
    private static final Identifier identifier = Main.locate("textures/entity/trail.png");

    public static void render(Trail trail, MultiBufferSource vertexConsumerProvider, PoseStack.Pose matrices) {
        if (trail.nullEntries >= trail.size || trail.entries == 0) {
            return;
        }

        VertexConsumer lineVertexConsumer = vertexConsumerProvider.getBuffer(RenderTypes.beaconBeam(identifier, true));
        int light = 15728640;

        Vec3 pos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        Matrix3f matrix = matrices.normal();

        //todo a custom vertex indexing methode would be beneficial here
        for (int i = 1; i < Math.min(trail.entries, trail.size); i++) {
            int pre = ((i + trail.lastIndex - 1) % trail.size) * 7;
            int index = ((i + trail.lastIndex) % trail.size) * 7;

            float a1 = (1.0f - (float) i / trail.size) * trail.buffer[pre + 6];
            float a2 = i == (trail.size - 1) ? 0.0f : (1.0f - ((float) i + 1.0f) / trail.size) * trail.buffer[index + 6];

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
        int gray = colorChannel(trail.gray);
        lineVertexConsumer.addVertex(p.x, p.y, p.z)
                .setColor(gray, gray, gray, colorChannel(a))
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(1.0f, 0.0f, 0.0f);
    }

    private static int colorChannel(float value) {
        return Mth.clamp((int) (value * 255.0f), 0, 255);
    }
}
