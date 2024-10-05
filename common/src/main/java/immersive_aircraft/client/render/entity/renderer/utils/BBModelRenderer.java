package immersive_aircraft.client.render.entity.renderer.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.resources.bbmodel.*;
import immersive_aircraft.util.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class BBModelRenderer {
    public static <T extends VehicleEntity> void renderModel(BBModel model, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float time, T entity, ModelPartRenderHandler<T> modelPartRenderer, float red, float green, float blue, float alpha) {
        model.root.forEach(object -> renderObject(model, object, matrixStack, vertexConsumerProvider, light, time, entity, modelPartRenderer, red, green, blue, alpha));
    }

    public static <T extends VehicleEntity> void renderObject(BBModel model, BBObject object, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float time, T entity, ModelPartRenderHandler<T> modelPartRenderer, float red, float green, float blue, float alpha) {
        matrixStack.pushPose();
        matrixStack.translate(object.origin.x(), object.origin.y(), object.origin.z());

        // Apply animations
        if (!model.animations.isEmpty()) {
            BBAnimation animation = model.animations.get(0);
            if (animation.hasAnimator(object.uuid)) {
                Vector3f position = animation.sample(object.uuid, BBAnimator.Channel.POSITION, time);
                position.mul(1.0f / 16.0f);
                matrixStack.translate(position.x(), position.y(), position.z());

                Vector3f rotation = animation.sample(object.uuid, BBAnimator.Channel.ROTATION, time);
                rotation.mul(1.0f / 180.0f * (float) Math.PI);
                matrixStack.mulPose(Utils.fromXYZ(rotation));

                Vector3f scale = animation.sample(object.uuid, BBAnimator.Channel.SCALE, time);
                matrixStack.scale(scale.x(), scale.y(), scale.z());
            }
        }

        // Apply object rotation
        matrixStack.mulPose(Utils.fromXYZ(object.rotation));

        // Apply additional, complex animations
        if (object instanceof BBBone bone && modelPartRenderer != null) {
            modelPartRenderer.animate(bone.name, entity, matrixStack, time);
        }

        // The bones origin is only used during transformation
        if (object instanceof BBBone) {
            matrixStack.translate(-object.origin.x(), -object.origin.y(), -object.origin.z());
        }

        // Render the object
        if (modelPartRenderer == null || !modelPartRenderer.render(object.name, model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer)) {
            renderObjectInner(model, object, matrixStack, vertexConsumerProvider, light, time, entity, modelPartRenderer, red, green, blue, alpha);
        }

        matrixStack.popPose();
    }

    public static <T extends VehicleEntity> void renderObjectInner(BBModel model, BBObject object, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float time, T entity, ModelPartRenderHandler<T> modelPartRenderer, float red, float green, float blue, float alpha) {
        if (object instanceof BBFaceContainer cube) {
            renderFaces(cube, matrixStack, vertexConsumerProvider, light, red, green, blue, alpha, null);
        } else if (object instanceof BBBone bone) {
            boolean shouldRender = bone.visibility;
            if (bone.name.equals("lod0")) {
                shouldRender = entity.isWithinParticleRange();
            } else if (bone.name.equals("lod1")) {
                shouldRender = !entity.isWithinParticleRange();
            }

            if (shouldRender) {
                bone.children.forEach(child -> renderObject(model, child, matrixStack, vertexConsumerProvider, light, time, entity, modelPartRenderer, red, green, blue, alpha));
            }
        }
    }

    public static void renderFaces(BBFaceContainer cube, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float red, float green, float blue, float alpha, VertexConsumer overrideVertexConsumer) {
        PoseStack.Pose last = matrixStack.last();
        Matrix4f positionMatrix = last.pose();
        Matrix3f normalMatrix = last.normal();
        for (BBFace face : cube.getFaces()) {
            VertexConsumer vertexConsumer = overrideVertexConsumer == null ? vertexConsumerProvider.getBuffer(cube.enableCulling() ? RenderType.entityCutout(face.texture.location) : RenderType.entityCutoutNoCull(face.texture.location)) : overrideVertexConsumer;
            for (int i = 0; i < 4; i++) {
                BBFace.BBVertex v = face.vertices[i];
                Vector3f n = normalMatrix.transform(v.nx, v.ny, v.nz, new Vector3f());
                vertexConsumer.addVertex(positionMatrix, v.x, v.y, v.z)
                        .setColor(red, green, blue, alpha)
                        .setUv(v.u, v.v)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(light)
                        .setNormal(n.x, n.y, n.z);
            }
        }
    }

    public static void renderBanner(BBFaceContainer cube, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light, boolean isBanner, List<BannerPatternLayers.Layer> patterns) {
        matrixStack.pushPose();

        if (cube instanceof BBObject object) {
            matrixStack.translate(object.origin.x(), object.origin.y(), object.origin.z());
        }

        for (BannerPatternLayers.Layer pattern : patterns) {
            Material material = isBanner ? Sheets.getBannerMaterial(pattern.pattern()) : Sheets.getShieldMaterial(pattern.pattern());
            VertexConsumer vertexConsumer = material.buffer(vertexConsumers, RenderType::entityNoOutline);
            int fs = pattern.color().getTextureDiffuseColor();
            float r = ((fs >> 16) & 0xFF) / 255.0f;
            float g = ((fs >> 8) & 0xFF) / 255.0f;
            float b = (fs & 0xFF) / 255.0f;
            renderFaces(cube, matrixStack, vertexConsumers, light,
                    r, g, b, 1.0f,
                    vertexConsumer);
        }

        matrixStack.popPose();
    }

    public static void renderSailObject(BBMesh cube, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float time, float red, float green, float blue, float alpha) {
        renderSailObject(cube, matrixStack, vertexConsumerProvider, light, time, red, green, blue, alpha, 0.025f, 0.0f);
    }

    public static void renderSailObject(BBMesh cube, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float time, float red, float green, float blue, float alpha, float distanceScale, float baseScale) {
        PoseStack.Pose last = matrixStack.last();
        Matrix4f positionMatrix = last.pose();
        Matrix3f normalMatrix = last.normal();
        for (BBFace face : cube.getFaces()) {
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityCutoutNoCull(face.texture.location));
            for (int i = 0; i < 4; i++) {
                BBFace.BBVertex v = face.vertices[i];
                float distance = Math.max(
                        Math.max(
                                Math.abs(v.x),
                                Math.abs(v.y)
                        ),
                        Math.abs(v.z)
                );
                double angle = (v.x + v.z + v.y * 0.25) * 4.0f + time * 4.0f;
                double scale = distanceScale * distance + baseScale;
                float x = (float) ((Math.cos(angle) + Math.cos(angle * 1.7)) * scale);
                float z = (float) ((Math.sin(angle) + Math.sin(angle * 1.7)) * scale);
                Vector3f n = normalMatrix.transform(v.nx, v.ny, v.nz, new Vector3f());

                vertexConsumer
                        .addVertex(positionMatrix, v.x + x, v.y, v.z + z)
                        .setColor(red, green, blue, alpha)
                        .setUv(v.u, v.v)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(light)
                        .setNormal(n.x, n.y, n.z);
            }
        }
    }
}
