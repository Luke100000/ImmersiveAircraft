package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import immersive_aircraft.Main;
import immersive_aircraft.entity.misc.Trail;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class TrailRenderer {
    private static final ResourceLocation identifier = Main.locate("textures/entity/trail.png");

    public static void render(Trail trail,
                              SubmitNodeCollector submitNodeCollector,
                              PoseStack poseStack,
                              CameraRenderState cameraRenderState,
                              PoseStack.Pose peek) {
        if (trail.nullEntries >= trail.size || trail.entries == 0) {
            return;
        }

        submitNodeCollector.submitCustomGeometry(poseStack, RenderType.beaconBeam(identifier, true), ((_pose, lineVertexConsumer) -> {
            int light = 15728640;

            Vec3 pos = cameraRenderState.pos;
            Matrix3f normal = peek.normal();

            //todo a custom vertex indexing methode would be beneficial here
            for (int i = 1; i < Math.min(trail.entries, trail.size); i++) {
                int pre = ((i + trail.lastIndex - 1) % trail.size) * 7;
                int index = ((i + trail.lastIndex) % trail.size) * 7;

                float a1 = ((1.0f - ((float) (trail.size - i) / trail.size)) * trail.buffer[pre + 6]);
                float a2 = i == (trail.size - 1) ? 0 : ((1.0f - ((float) (trail.size - i) / trail.size)) * trail.buffer[pre + 6]);

                vertex(trail, lineVertexConsumer, normal, 0, 0, pre, pos, a1, light);
                vertex(trail, lineVertexConsumer, normal, 0, 1, pre + 3, pos, a1, light);
                vertex(trail, lineVertexConsumer, normal, 1, 1, index + 3, pos, a2, light);
                vertex(trail, lineVertexConsumer, normal, 1, 0, index, pos, a2, light);

                //todo the anti culling here is stupid
                vertex(trail, lineVertexConsumer, normal, 1, 0, index, pos, a2, light);
                vertex(trail, lineVertexConsumer, normal, 1, 1, index + 3, pos, a2, light);
                vertex(trail, lineVertexConsumer, normal, 0, 1, pre + 3, pos, a1, light);
                vertex(trail, lineVertexConsumer, normal, 0, 0, pre, pos, a1, light);
            }
        }));
    }

    private static void vertex(Trail trail, VertexConsumer lineVertexConsumer, Matrix3f matrix, float u, float v, int index, Vec3 pos, float a, int light) {
        Vector3f p = new Vector3f((float) (trail.buffer[index] - pos.x), (float) (trail.buffer[index + 1] - pos.y), (float) (trail.buffer[index + 2] - pos.z));
        matrix.transform(p);
        lineVertexConsumer.addVertex(p.x, p.y, p.z)
                .setColor(trail.gray, trail.gray, trail.gray, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(1, 0, 0);
    }
}
