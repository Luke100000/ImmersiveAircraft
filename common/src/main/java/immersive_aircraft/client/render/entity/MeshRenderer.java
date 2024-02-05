package immersive_aircraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import immersive_aircraft.resources.ObjectLoader;
import immersive_aircraft.resources.obj.Face;
import immersive_aircraft.resources.obj.FaceVertex;
import immersive_aircraft.resources.obj.Mesh;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

public interface MeshRenderer {
    static Mesh getFaces(ResourceLocation id, String object) {
        return ObjectLoader.objects.get(id).get(object);
    }

    static void renderObject(Mesh mesh, PoseStack matrixStack, VertexConsumer vertexConsumer, int light) {
        renderObject(mesh, matrixStack, vertexConsumer, light, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    static void renderObject(Mesh mesh, PoseStack matrixStack, VertexConsumer vertexConsumer, int light, float r, float g, float b, float a) {
        PoseStack.Pose entry = matrixStack.last();
        Matrix4f positionMatrix = entry.pose();
        Matrix3f normalMatrix = entry.normal();
        for (Face face : mesh.faces) {
            if (face.vertices.size() == 4) {
                for (FaceVertex v : face.vertices) {
                    vertexConsumer
                            .vertex(positionMatrix, v.v.x, v.v.y, v.v.z)
                            .color(r, g, b, a)
                            .uv(v.t.u, v.t.v)
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(light)
                            .normal(normalMatrix, v.n.x, v.n.y, v.n.z)
                            .endVertex();
                }
            }
        }
    }

    static void renderSailObject(Mesh mesh, PoseStack matrixStack, VertexConsumer vertexConsumer, int light, double time, float r, float g, float b, float a) {
        PoseStack.Pose entry = matrixStack.last();
        Matrix4f positionMatrix = entry.pose();
        Matrix3f normalMatrix = entry.normal();
        for (Face face : mesh.faces) {
            if (face.vertices.size() == 4) {
                for (FaceVertex v : face.vertices) {
                    double angle = v.v.x + v.v.z + v.v.y * 0.25 + time * 0.25;
                    double scale = 0.05;
                    float x = (float) (v.v.x + (Math.cos(angle) + Math.cos(angle * 1.7)) * scale * v.c.r);
                    float z = (float) (v.v.z + (Math.sin(angle) + Math.sin(angle * 1.7)) * scale * v.c.r);
                    vertexConsumer
                            .vertex(positionMatrix, x, v.v.y, z)
                            .color(r, g, b, a)
                            .uv(v.t.u, v.t.v)
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(light)
                            .normal(normalMatrix, v.n.x, v.n.y, v.n.z)
                            .endVertex();
                }
            }
        }
    }

    static void renderBanner(PoseStack matrixStack, MultiBufferSource vertexConsumers, int light, Mesh mesh, boolean isBanner, List<Pair<Holder<BannerPattern>, DyeColor>> patterns) {
        for (int i = 0; i < 17 && i < patterns.size(); ++i) {
            Pair<Holder<BannerPattern>, DyeColor> pair = patterns.get(i);
            float[] fs = pair.getSecond().getTextureDiffuseColors();
            Holder<BannerPattern> bannerPattern = pair.getFirst();
            bannerPattern.unwrapKey().ifPresent(key -> {
                Material spriteIdentifier = isBanner ? Sheets.getBannerMaterial(key) : Sheets.getShieldMaterial(key);
                VertexConsumer vertexConsumer = spriteIdentifier.buffer(vertexConsumers, RenderType::entityNoOutline);
                TextureAtlasSprite sprite = spriteIdentifier.sprite();
                PoseStack.Pose entry = matrixStack.last();
                Matrix4f positionMatrix = entry.pose();
                Matrix3f normalMatrix = entry.normal();
                for (Face face : mesh.faces) {
                    if (face.vertices.size() == 4) {
                        for (FaceVertex v : face.vertices) {
                            vertexConsumer
                                    .vertex(positionMatrix, v.v.x, v.v.y, v.v.z)
                                    .color(fs[0], fs[1], fs[2], 1.0f)
                                    .uv(v.t.u * (sprite.getU1() - sprite.getU0()) + sprite.getU0(), v.t.v * (sprite.getV1() - sprite.getV0()) + sprite.getV0())
                                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                                    .uv2(light)
                                    .normal(normalMatrix, v.n.x, v.n.y, v.n.z)
                                    .endVertex();
                        }
                    }
                }
            });
        }
    }
}
