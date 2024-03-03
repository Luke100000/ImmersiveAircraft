package immersive_aircraft.client.render.entity.renderer.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.resources.bbmodel.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Holder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;

import java.util.List;

public class BBModelRenderer {
    public static <T extends AircraftEntity> void renderModel(BBModel model, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float time, T entity, ModelPartRenderHandler<T> modelPartRenderer, float red, float green, float blue, float alpha) {
        model.root.forEach(object -> renderObject(model, object, matrixStack, vertexConsumerProvider, light, time, entity, modelPartRenderer, red, green, blue, alpha));
    }

    public static <T extends AircraftEntity> void renderObject(BBModel model, BBObject object, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float time, T entity, ModelPartRenderHandler<T> modelPartRenderer, float red, float green, float blue, float alpha) {
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
                matrixStack.mulPose(Quaternion.fromXYZ(rotation));

                Vector3f scale = animation.sample(object.uuid, BBAnimator.Channel.SCALE, time);
                matrixStack.scale(scale.x(), scale.y(), scale.z());
            }
        }

        matrixStack.mulPose(Quaternion.fromXYZ(object.rotation));

        if (object instanceof BBBone bone && modelPartRenderer != null) {
            modelPartRenderer.animate(bone.name, entity, matrixStack, time);
        }

        if (object instanceof BBBone) {
            matrixStack.translate(-object.origin.x(), -object.origin.y(), -object.origin.z());
        }

        if (modelPartRenderer == null || !modelPartRenderer.render(object.name, model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer)) {
            if (object instanceof BBFaceContainer cube) {
                renderFaces(cube, matrixStack, vertexConsumerProvider, light, red, green, blue, alpha, 1.0f, 1.0f, 0.0f, 0.0f, null);
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

        matrixStack.popPose();
    }

    public static void renderFaces(BBFaceContainer cube, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float red, float green, float blue, float alpha, float uScale, float vScale, float uOffset, float vOffset, VertexConsumer overrideVertexConsumer) {
        PoseStack.Pose last = matrixStack.last();
        Matrix4f positionMatrix = last.pose();
        Matrix3f normalMatrix = last.normal();
        for (BBFace face : cube.getFaces()) {
            VertexConsumer vertexConsumer = overrideVertexConsumer == null ? vertexConsumerProvider.getBuffer(RenderType.entityCutout(face.texture.location)) : overrideVertexConsumer;
            for (int i = 0; i < 4; i++) {
                BBFace.BBVertex v = face.vertices[i];
                vertexConsumer
                        .vertex(positionMatrix, v.x, v.y, v.z)
                        .color(red, green, blue, alpha)
                        .uv(v.u * uScale + uOffset, v.v * vScale + vOffset)
                        .overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(light)
                        .normal(normalMatrix, v.nx, v.ny, v.nz)
                        .endVertex();
            }
        }
    }

    public static void renderBanner(BBFaceContainer cube, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light, boolean isBanner, List<Pair<Holder<BannerPattern>, DyeColor>> patterns) {
        for (int i = 0; i < 17 && i < patterns.size(); ++i) {
            Pair<Holder<BannerPattern>, DyeColor> pair = patterns.get(i);
            float[] fs = pair.getSecond().getTextureDiffuseColors();
            Holder<BannerPattern> bannerPattern = pair.getFirst();
            bannerPattern.unwrapKey().ifPresent(key -> {
                Material spriteIdentifier = isBanner ? Sheets.getBannerMaterial(key) : Sheets.getShieldMaterial(key);
                VertexConsumer vertexConsumer = spriteIdentifier.buffer(vertexConsumers, RenderType::entityNoOutline);
                TextureAtlasSprite sprite = spriteIdentifier.sprite();
                renderFaces(cube, matrixStack, vertexConsumers, light, fs[0], fs[1], fs[2], 1.0f, (sprite.getU1() - sprite.getU0()), (sprite.getV1() - sprite.getV0()), sprite.getU0(), sprite.getV0(), vertexConsumer);
            });
        }
    }

    public static void renderSailObject(BBMesh cube, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float time, float red, float green, float blue, float alpha) {
        PoseStack.Pose last = matrixStack.last();
        Matrix4f positionMatrix = last.pose();
        Matrix3f normalMatrix = last.normal();
        for (BBFace face : cube.getFaces()) {
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityCutoutNoCull(face.texture.location));
            for (int i = 0; i < 4; i++) {
                BBFace.BBVertex v = face.vertices[i];

                float distance = (float) Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
                double angle = (v.x + v.z + v.y * 0.25) * 4.0f + time * 4.0f;
                double scale = 0.025 * distance;
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
