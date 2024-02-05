package immersive_aircraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import immersive_aircraft.Main;
import immersive_aircraft.resources.bbmodel.*;
import immersive_aircraft.util.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BBModelRenderer {
    public static void renderModel(BBModel model, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float time) {
        model.root.forEach(object -> {
            renderObject(model, object, matrixStack, vertexConsumerProvider, light, time);
        });
    }

    private static void renderObject(BBModel model, BBObject object, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float time) {
        matrixStack.pushPose();
        matrixStack.translate(object.origin.x(), object.origin.y(), object.origin.z());

        if (!model.animations.isEmpty()) {
            BBAnimation animation = model.animations.get(0);
            if (animation.hasAnimator(object.uuid)) {
                Vector3f position = animation.sample(object.uuid, BBAnimator.Channel.POSITION, time);
                position.mul(1.0f / 16.0f);
                matrixStack.translate(position.x(), position.y(), position.z());

                Vector3f rotation = animation.sample(object.uuid, BBAnimator.Channel.ROTATION, time);
                rotation.mul(1.0f / 180.0f * (float) Math.PI);
                matrixStack.mulPose(Utils.fromXYZ(rotation.x(), rotation.y(), rotation.z()));

                Vector3f scale = animation.sample(object.uuid, BBAnimator.Channel.SCALE, time);
                matrixStack.scale(scale.x(), scale.y(), scale.z());
            }
        }

        matrixStack.mulPose(Utils.fromXYZ(object.rotation.x(), object.rotation.y(), object.rotation.z()));
        matrixStack.translate(-object.origin.x(), -object.origin.y(), -object.origin.z());

        if (object instanceof BBCube cube) {
            renderCube(cube, matrixStack, vertexConsumerProvider, light);
        } else if (object instanceof BBBone bone) {
            bone.children.forEach(child -> {
                renderObject(model, child, matrixStack, vertexConsumerProvider, light, time);
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
