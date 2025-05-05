package immersive_aircraft.client.render.entity.renderer.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.resources.bbmodel.*;
import immersive_aircraft.util.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class BBModelRenderer {
    public interface VertexConsumerProvider {
        VertexConsumer getBuffer(MultiBufferSource source, BBFaceContainer container, BBFace face);
    }

    public static final VertexConsumerProvider DEFAULT_VERTEX_CONSUMER_PROVIDER = (source, container, face) -> source.getBuffer(container.enableCulling() ? RenderType.entityCutout(face.texture.location) : RenderType.entityCutoutNoCull(face.texture.location));

    public static <T extends VehicleEntity> void renderModel(BBModel model, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float time, T entity, ModelPartRenderHandler<T> modelPartRenderer, float red, float green, float blue, float alpha) {
        model.root.forEach(object -> renderObject(model, object, matrixStack, vertexConsumerProvider, light, time, entity, modelPartRenderer, red, green, blue, alpha));
    }

    /**
     * Apply transformations, animations, and callbacks, and render the object.
     */
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

    /**
     * Render the object without applying transformations, animations, or callbacks.
     */
    public static <T extends VehicleEntity> void renderObjectInner(BBModel model, BBObject object, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float time, T entity, ModelPartRenderHandler<T> modelPartRenderer, float red, float green, float blue, float alpha) {
        if (object instanceof BBFaceContainer cube) {
            renderFaces(cube, matrixStack, vertexConsumerProvider, light, red, green, blue, alpha, modelPartRenderer == null ? DEFAULT_VERTEX_CONSUMER_PROVIDER : modelPartRenderer.getVertexConsumerProvider());
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

    public static void renderFaces(BBFaceContainer cube, PoseStack matrixStack, MultiBufferSource source, int light, float red, float green, float blue, float alpha, VertexConsumerProvider provider) {
        PoseStack.Pose last = matrixStack.last();
        Matrix4f positionMatrix = last.pose();
        Matrix3f normalMatrix = last.normal();
        for (BBFace face : cube.getFaces()) {
            VertexConsumer vertexConsumer = provider.getBuffer(source, cube, face);
            for (int i = 0; i < 4; i++) {
                BBFace.BBVertex v = face.vertices[i];
                vertexConsumer.vertex(positionMatrix, v.x, v.y, v.z);
                vertexConsumer.color(red, green, blue, alpha);
                vertexConsumer.uv(v.u, v.v);
                vertexConsumer.overlayCoords(OverlayTexture.NO_OVERLAY);
                vertexConsumer.uv2(light);
                vertexConsumer.normal(normalMatrix, v.nx, v.ny, v.nz);
                vertexConsumer.endVertex();
            }
        }
    }

    public static void renderBanner(BBFaceContainer cube, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light, boolean isBanner, List<Pair<Holder<BannerPattern>, DyeColor>> patterns) {
        matrixStack.pushPose();

        if (cube instanceof BBObject object) {
            matrixStack.translate(object.origin.x(), object.origin.y(), object.origin.z());
        }

        for (int i = 0; i < 17 && i < patterns.size(); ++i) {
            Pair<Holder<BannerPattern>, DyeColor> pair = patterns.get(i);
            Holder<BannerPattern> bannerPattern = pair.getFirst();
            bannerPattern.unwrapKey()
                    .map(key -> isBanner ? Sheets.getBannerMaterial(key) : Sheets.getShieldMaterial(key))
                    .ifPresent(material -> {
                        float[] fs = pair.getSecond().getTextureDiffuseColors();
                        renderFaces(cube, matrixStack, vertexConsumers, light,
                                fs[0], fs[1], fs[2], 1.0f,
                                (source, container, face) -> material.buffer(vertexConsumers, RenderType::entityNoOutline));
                    });
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

                vertexConsumer
                        .vertex(positionMatrix, v.x + x, v.y, v.z + z)
                        .color(red, green, blue, alpha)
                        .uv(v.u, v.v)
                        .overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(light)
                        .normal(normalMatrix, v.nx, v.ny, v.nz)
                        .endVertex();
            }
        }
    }
}
