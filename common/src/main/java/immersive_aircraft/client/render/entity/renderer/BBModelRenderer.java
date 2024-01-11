package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import immersive_aircraft.Main;
import immersive_aircraft.resources.bbmodel.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BBModelRenderer {
    public static void renderModel(BBModel model, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light) {
        model.root.forEach(object -> {
            renderObject(object, matrixStack, vertexConsumerProvider, light);
        });
    }

    private static void renderObject(BBObject object, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light) {
        matrixStack.pushPose();
        Quaternion rotation = Quaternion.fromXYZ(object.rotation);
        matrixStack.translate(object.origin.x(), object.origin.y(), object.origin.z());
        matrixStack.mulPose(rotation);

        // if animation is not null, apply animation (scale, rotation, translation)

        matrixStack.translate(-object.origin.x(), -object.origin.y(), -object.origin.z());

        if (object instanceof BBCube cube) {
            renderCube(cube, matrixStack, vertexConsumerProvider, light);
        } else if (object instanceof BBBone bone) {
            bone.children.forEach(child -> {
                renderObject(child, matrixStack, vertexConsumerProvider, light);
            });
        }
        matrixStack.popPose();
    }

    private static void renderCube(BBCube cube, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light) {
        PoseStack.Pose last = matrixStack.last();
        Matrix4f positionMatrix = last.pose();
        Matrix3f normalMatrix = last.normal();
        for (BBFace face : cube.faces) {
            ResourceLocation id = Main.locate("textures/" + face.texture.name);
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityCutout(id));
            for (int i = 0; i < 4; i++) {
                BBFace.BBVertex v = face.vertices[i];
                vertexConsumer
                        .vertex(positionMatrix, v.x, v.y, v.z)
                        .color(1.0f, 1.0f, 1.0f, 1.0f)
                        .uv(v.u, v.v)
                        .overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(light)
                        .normal(normalMatrix, v.nx, v.ny, v.nz)
                        .endVertex();
            }
        }
    }
}
