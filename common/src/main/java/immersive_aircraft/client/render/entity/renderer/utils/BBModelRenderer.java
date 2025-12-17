package immersive_aircraft.client.render.entity.renderer.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.render.entity.renderer.VehicleEntityRenderState;
import immersive_aircraft.resources.bbmodel.*;
import immersive_aircraft.util.Utils;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiFunction;

public class BBModelRenderer {
    public static final BiFunction<BBFaceContainer, BBFace, RenderType> DEFAULT_RENDER_TYPE = (container, face) -> container.enableCulling() ? RenderType.entityCutout(face.texture.location) : RenderType.entityCutoutNoCull(face.texture.location);

    public static <S extends VehicleEntityRenderState> void renderModel(BBModel model,
                                                                        ModelPartRenderHandler<S> modelPartRenderHandler,
                                                                        PoseStack matrixStack,
                                                                        S entity,
                                                                        SubmitNodeCollector submitNodeCollector,
                                                                        float red,
                                                                        float green,
                                                                        float blue,
                                                                        float alpha) {
        model.root.forEach(object -> renderObject(model, object, matrixStack, entity, submitNodeCollector, modelPartRenderHandler, red, green, blue, alpha));
    }

    /**
     * Apply transformations, animations, and callbacks, and render the object.
     */
    public static <S extends VehicleEntityRenderState> void renderObject(BBModel model,
                                                                         BBObject object,
                                                                         PoseStack matrixStack,
                                                                         S entity,
                                                                         SubmitNodeCollector submitNodeCollector,
                                                                         ModelPartRenderHandler<S> modelPartRenderHandler,
                                                                         float red,
                                                                         float green,
                                                                         float blue,
                                                                         float alpha) {
        matrixStack.pushPose();
        matrixStack.translate(object.origin.x(), object.origin.y(), object.origin.z());

        // Apply animations
        if (!model.animations.isEmpty()) {
            BBAnimation animation = model.animations.getFirst();
            if (animation.hasAnimator(object.uuid)) {
                Vector3f position = animation.sample(object.uuid, BBAnimator.Channel.POSITION, entity.time, entity.animationVariables);
                position.mul(1.0f / 16.0f);
                matrixStack.translate(position.x(), position.y(), position.z());

                Vector3f rotation = animation.sample(object.uuid, BBAnimator.Channel.ROTATION, entity.time, entity.animationVariables);
                rotation.mul(1.0f / 180.0f * (float) Math.PI);
                matrixStack.mulPose(Utils.fromXYZ(rotation));

                Vector3f scale = animation.sample(object.uuid, BBAnimator.Channel.SCALE, entity.time, entity.animationVariables);
                matrixStack.scale(scale.x(), scale.y(), scale.z());
            }
        }

        // Apply object rotation
        matrixStack.mulPose(Utils.fromXYZ(object.rotation));

        // Apply additional, complex animations
        if (object instanceof BBBone bone && modelPartRenderHandler != null) {
            modelPartRenderHandler.animate(bone.name, entity, matrixStack, entity.time);
        }

        // The bones origin is only used during transformation
        if (object instanceof BBBone) {
            matrixStack.translate(-object.origin.x(), -object.origin.y(), -object.origin.z());
        }

        // Render the object
        if (modelPartRenderHandler == null || !modelPartRenderHandler.render(object.name, model, object, submitNodeCollector, entity, matrixStack, modelPartRenderHandler)) {
            renderObjectInner(model, object, matrixStack, entity, submitNodeCollector, modelPartRenderHandler, red, green, blue, alpha);
        }

        matrixStack.popPose();
    }

    /**
     * Render the object without applying transformations, animations, or callbacks.
     */
    public static <T extends VehicleEntityRenderState> void renderObjectInner(BBModel model, BBObject object, PoseStack matrixStack, T entity, SubmitNodeCollector submitNodeCollector, ModelPartRenderHandler<T> modelPartRenderHandler, float red, float green, float blue, float alpha) {
        if (object instanceof BBFaceContainer cube) {
            renderFaces(cube, matrixStack, submitNodeCollector, entity.packedLight, red, green, blue, alpha);
        } else if (object instanceof BBBone bone) {
            boolean shouldRender = bone.visibility;
            if (bone.name.equals("lod0")) {
                shouldRender = entity.isWithinParticleRange;
            } else if (bone.name.equals("lod1")) {
                shouldRender = !entity.isWithinParticleRange;
            }

            if (shouldRender) {
                bone.children.forEach(child -> renderObject(model, child, matrixStack, entity, submitNodeCollector, modelPartRenderHandler, red, green, blue, alpha));
            }
        }
    }

    public static void renderFaces(BBFaceContainer cube, PoseStack matrixStack, SubmitNodeCollector submitNodeCollector, int light, float red, float green, float blue, float alpha) {
        for (BBFace face : cube.getFaces()) {
            submitNodeCollector.submitCustomGeometry(
                    matrixStack,
                    DEFAULT_RENDER_TYPE.apply(cube, face),
                    (pose, vertexConsumer) -> {
                        Matrix4f positionMatrix = pose.pose();
                        Matrix3f normalMatrix = pose.normal();
                        for (int i = 0; i < 4; i++) {
                            BBFace.BBVertex v = face.vertices[i];
                            Vector3f p = positionMatrix.transformPosition(v.x, v.y, v.z, new Vector3f());
                            Vector3f n = normalMatrix.transform(v.nx, v.ny, v.nz, new Vector3f());
                            int color = ARGB.colorFromFloat(alpha, red, green, blue);
                                            vertexConsumer.addVertex(p.x, p.y, p.z, color, v.u, v.v, OverlayTexture.NO_OVERLAY, light, n.x, n.y, n.z);
                        }
            });
        }
    }

    public static void renderBanner(BBFaceContainer cube,
                                    PoseStack matrixStack,
                                    SubmitNodeCollector submitNodeCollector,
                                    int light,
                                    boolean isBanner,
                                    DyeColor baseColor,
                                    List<BannerPatternLayers.Layer> patterns) {
        matrixStack.pushPose();

        if (cube instanceof BBObject object) {
            matrixStack.translate(object.origin.x(), object.origin.y(), object.origin.z());
        }

        // Render the base material
        Material baseMaterial = isBanner ? Sheets.BANNER_BASE : Sheets.SHIELD_BASE;
        renderBannerMaterial(cube, matrixStack, submitNodeCollector, light, baseColor, baseMaterial);

        // And the patterns
        for (BannerPatternLayers.Layer pattern : patterns) {
            Material material = isBanner ? Sheets.getBannerMaterial(pattern.pattern()) : Sheets.getShieldMaterial(pattern.pattern());
            renderBannerMaterial(cube, matrixStack, submitNodeCollector, light, pattern.color(), material);
        }

        matrixStack.popPose();
    }

    private static void renderBannerMaterial(BBFaceContainer cube, PoseStack matrixStack, SubmitNodeCollector submitNodeCollector, int light, DyeColor color, Material material) {
        int fs = color.getTextureDiffuseColor();
        float r = ((fs >> 16) & 0xFF) / 255.0f;
        float g = ((fs >> 8) & 0xFF) / 255.0f;
        float b = (fs & 0xFF) / 255.0f;
        for (BBFace face : cube.getFaces()) {
            submitNodeCollector.submitCustomGeometry(
                    matrixStack,
                    // TODO: May require actually creating a material buffer.
                    material.renderType(RenderType::entityNoOutline),
                    (pose, vertexConsumer) -> {
                        Matrix4f positionMatrix = pose.pose();
                        Matrix3f normalMatrix = pose.normal();
                        for (int i = 0; i < 4; i++) {
                            BBFace.BBVertex v = face.vertices[i];
                            Vector3f p = positionMatrix.transformPosition(v.x, v.y, v.z, new Vector3f());
                            Vector3f n = normalMatrix.transform(v.nx, v.ny, v.nz, new Vector3f());
                            int c = ARGB.colorFromFloat(1.0f, r, g, b);
                            vertexConsumer.addVertex(p.x, p.y, p.z, c, v.u, v.v, OverlayTexture.NO_OVERLAY, light, n.x, n.y, n.z);
                        }
                    });
        }
    }

    public static void renderSailObject(BBMesh cube,
                                        PoseStack matrixStack,
                                        SubmitNodeCollector submitNodeCollector,
                                        int light,
                                        float time,
                                        float red,
                                        float green,
                                        float blue,
                                        float alpha) {
        renderSailObject(cube, matrixStack, submitNodeCollector, light, time, red, green, blue, alpha, 0.025f, 0.0f);
    }

    public static void renderSailObject(BBMesh cube,
                                        PoseStack matrixStack,
                                        SubmitNodeCollector submitNodeCollector,
                                        int light,
                                        float time,
                                        float red,
                                        float green,
                                        float blue,
                                        float alpha,
                                        float distanceScale,
                                        float baseScale) {
        for (BBFace face : cube.getFaces()) {
            submitNodeCollector.submitCustomGeometry(matrixStack,
                    RenderType.entityCutoutNoCull(face.texture.location),
                    (pose, vertexConsumer) -> {
                        Matrix4f positionMatrix = pose.pose();
                        Matrix3f normalMatrix = pose.normal();
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

                    });
        }
    }
}
