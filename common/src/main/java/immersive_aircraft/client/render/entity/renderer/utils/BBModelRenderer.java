package immersive_aircraft.client.render.entity.renderer.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.resources.bbmodel.*;
import immersive_aircraft.util.Utils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class BBModelRenderer {
    public interface RenderTypeProvider {
        RenderType getRenderType(BBFaceContainer container, BBFace face);
    }

    public static final RenderTypeProvider DEFAULT_RENDER_TYPE_PROVIDER = (container, face) ->
            RenderTypes.entityCutout(face.texture.location, container.enableCulling());

    public static <T extends VehicleEntity> void renderModel(BBModel model, PoseStack matrixStack, SubmitNodeCollector collector, int light, float time, T entity, ModelPartRenderHandler<T> modelPartRenderer, float red, float green, float blue, float alpha) {
        model.root.forEach(object -> renderObject(model, object, matrixStack, collector, light, time, entity, modelPartRenderer, red, green, blue, alpha));
    }

    /**
     * Apply transformations, animations, and callbacks, and render the object.
     */
    public static <T extends VehicleEntity> void renderObject(BBModel model, BBObject object, PoseStack matrixStack, SubmitNodeCollector collector, int light, float time, T entity, ModelPartRenderHandler<T> modelPartRenderer, float red, float green, float blue, float alpha) {
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
        if (modelPartRenderer == null || !modelPartRenderer.render(object.name, model, object, collector, entity, matrixStack, light, time, modelPartRenderer)) {
            renderObjectInner(model, object, matrixStack, collector, light, time, entity, modelPartRenderer, red, green, blue, alpha);
        }

        matrixStack.popPose();
    }

    /**
     * Render the object without applying transformations, animations, or callbacks.
     */
    public static <T extends VehicleEntity> void renderObjectInner(BBModel model, BBObject object, PoseStack matrixStack, SubmitNodeCollector collector, int light, float time, T entity, ModelPartRenderHandler<T> modelPartRenderer, float red, float green, float blue, float alpha) {
        if (object instanceof BBFaceContainer cube) {
            renderFaces(cube, matrixStack, collector, light, red, green, blue, alpha, modelPartRenderer == null ? DEFAULT_RENDER_TYPE_PROVIDER : modelPartRenderer.getRenderTypeProvider());
        } else if (object instanceof BBBone bone) {
            boolean shouldRender = bone.visibility;
            if (bone.name.equals("lod0")) {
                shouldRender = entity.isWithinParticleRange();
            } else if (bone.name.equals("lod1")) {
                shouldRender = !entity.isWithinParticleRange();
            }

            if (shouldRender) {
                bone.children.forEach(child -> renderObject(model, child, matrixStack, collector, light, time, entity, modelPartRenderer, red, green, blue, alpha));
            }
        }
    }

    public static void renderFaces(BBFaceContainer cube, PoseStack matrixStack, SubmitNodeCollector collector, int light, float red, float green, float blue, float alpha, RenderTypeProvider provider) {
        for (BBFace face : cube.getFaces()) {
            RenderType renderType = provider.getRenderType(cube, face);
            collector.submitCustomGeometry(matrixStack, renderType, (pose, vertexConsumer) -> {
                Matrix4f positionMatrix = pose.pose();
                Matrix3f normalMatrix = pose.normal();
                int color = ARGB.colorFromFloat(alpha, red, green, blue);
                for (int i = 0; i < 4; i++) {
                    BBFace.BBVertex v = face.vertices[i];
                    Vector3f p = positionMatrix.transformPosition(v.x, v.y, v.z, new Vector3f());
                    Vector3f n = normalMatrix.transform(v.nx, v.ny, v.nz, new Vector3f());
                    vertexConsumer.addVertex(p.x, p.y, p.z, color, v.u, v.v, OverlayTexture.NO_OVERLAY, light, n.x, n.y, n.z);
                }
            });
        }
    }

    public static void renderBanner(BBFaceContainer cube, PoseStack matrixStack, SubmitNodeCollector collector, SpriteGetter spriteGetter, int light, boolean isBanner, DyeColor baseColor, List<BannerPatternLayers.Layer> patterns) {
        matrixStack.pushPose();

        if (cube instanceof BBObject object) {
            matrixStack.translate(object.origin.x(), object.origin.y(), object.origin.z());
        }

        // Render the base material
        SpriteId baseSprite = isBanner ? Sheets.BANNER_BASE : Sheets.BANNER_PATTERN_BASE;
        renderBannerSprite(cube, matrixStack, collector, spriteGetter, light, baseColor, baseSprite);

        // And the patterns
        for (BannerPatternLayers.Layer pattern : patterns) {
            SpriteId sprite = isBanner ? Sheets.getBannerSprite(pattern.pattern()) : Sheets.getShieldSprite(pattern.pattern());
            renderBannerSprite(cube, matrixStack, collector, spriteGetter, light, pattern.color(), sprite);
        }

        matrixStack.popPose();
    }

    private static void renderBannerSprite(BBFaceContainer cube, PoseStack matrixStack, SubmitNodeCollector collector, SpriteGetter spriteGetter, int light, DyeColor color, SpriteId spriteId) {
        int fs = color.getTextureDiffuseColor();
        float r = ((fs >> 16) & 0xFF) / 255.0f;
        float g = ((fs >> 8) & 0xFF) / 255.0f;
        float b = (fs & 0xFF) / 255.0f;
        RenderType renderType = spriteId.renderType(RenderTypes::bannerPattern);
        TextureAtlasSprite sprite = spriteGetter.get(spriteId);

        collector.submitCustomGeometry(matrixStack, renderType, (pose, vertexConsumer) -> {
            VertexConsumer wrapped = sprite.wrap(vertexConsumer);
            Matrix4f positionMatrix = pose.pose();
            Matrix3f normalMatrix = pose.normal();
            int colorInt = ARGB.colorFromFloat(1.0f, r, g, b);
            for (BBFace face : cube.getFaces()) {
                for (int i = 0; i < 4; i++) {
                    BBFace.BBVertex v = face.vertices[i];
                    Vector3f p = positionMatrix.transformPosition(v.x, v.y, v.z, new Vector3f());
                    Vector3f n = normalMatrix.transform(v.nx, v.ny, v.nz, new Vector3f());
                    wrapped.addVertex(p.x, p.y, p.z, colorInt, v.u, v.v, OverlayTexture.NO_OVERLAY, light, n.x, n.y, n.z);
                }
            }
        });
    }

    public static void renderSailObject(BBMesh cube, PoseStack matrixStack, SubmitNodeCollector collector, int light, float time, float red, float green, float blue, float alpha) {
        renderSailObject(cube, matrixStack, collector, light, time, red, green, blue, alpha, 0.025f, 0.0f);
    }

    public static void renderSailObject(BBMesh cube, PoseStack matrixStack, SubmitNodeCollector collector, int light, float time, float red, float green, float blue, float alpha, float distanceScale, float baseScale) {
        for (BBFace face : cube.getFaces()) {
            RenderType renderType = RenderTypes.entityCutout(face.texture.location, false);
            collector.submitCustomGeometry(matrixStack, renderType, (pose, vertexConsumer) -> {
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
