package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.resources.ObjectLoader;
import immersive_aircraft.util.obj.Face;
import immersive_aircraft.util.obj.FaceVertex;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.LinkedList;
import java.util.List;

public abstract class AircraftEntityRenderer<T extends AircraftEntity> extends EntityRenderer<T> {
    protected class Object {
        public interface AnimationConsumer<T> {
            void run(T entity, float yaw, float tickDelta, PoseStack matrixStack);
        }

        public interface RenderConsumer<T> {
            void run(MultiBufferSource vertexConsumerProvider, T entity, PoseStack matrixStack, int light, float tickDelta);
        }

        public Object(ResourceLocation id, String object) {
            this.id = id;
            this.object = object;
        }

        private final ResourceLocation id;
        private final String object;

        private AnimationConsumer<T> animationConsumer = null;
        private RenderConsumer<T> renderConsumer = (vertexConsumerProvider, entity, matrixStack, light, tickDelta) -> {
            //Get vertex consumer
            ResourceLocation identifier = getTextureLocation(entity);
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityCutout(identifier));
            renderObject(getMesh(), matrixStack, vertexConsumer, light);
        };

        public Mesh getMesh() {
            Mesh mesh = getFaces(id, object);
            if (mesh == null) {
                throw new RuntimeException(String.format("Mesh %s in %s does not exist!", id, object));
            }
            return mesh;
        }

        public ResourceLocation getId() {
            return id;
        }

        public AnimationConsumer<T> getAnimationConsumer() {
            return animationConsumer;
        }

        public Object setAnimationConsumer(AnimationConsumer<T> animationConsumer) {
            this.animationConsumer = animationConsumer;
            return this;
        }

        public RenderConsumer<T> getRenderConsumer() {
            return renderConsumer;
        }

        public Object setRenderConsumer(RenderConsumer<T> renderConsumer) {
            this.renderConsumer = renderConsumer;
            return this;
        }
    }

    protected class Model {

        public Model() {
        }

        private final List<Object> objects = new LinkedList<>();

        public Model add(Object o) {
            objects.add(o);
            return this;
        }

        public List<Object> getObjects() {
            return objects;
        }
    }

    public AircraftEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    protected abstract Model getModel(AircraftEntity entity);

    protected abstract Vector3f getPivot(AircraftEntity entity);


    @Override
    public void render(T entity, float yaw, float tickDelta, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light) {
        PoseStack.Pose peek = matrixStack.last();

        matrixStack.pushPose();

        //Wobble
        float h = (float) entity.getDamageWobbleTicks() - tickDelta;
        float j = entity.getDamageWobbleStrength() - tickDelta;
        if (j < 0.0f) {
            j = 0.0f;
        }
        if (h > 0.0f) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(h) * h * j / 10.0f * (float) entity.getDamageWobbleSide()));
        }

        Vector3f effect = entity.isOnGround() ? new Vector3f(0.0f, 0.0f, 0.0f) : entity.getWindEffect();
        matrixStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        matrixStack.mulPose(Axis.XP.rotationDegrees(entity.getViewXRot(tickDelta) + effect.z));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(entity.getRoll(tickDelta) + effect.x));

        Vector3f pivot = getPivot(entity);
        matrixStack.translate(pivot.x, pivot.y, pivot.z);

        //Render parts
        Model model = getModel(entity);
        for (Object object : model.getObjects()) {
            if (object.getAnimationConsumer() != null) {
                matrixStack.pushPose();
                object.getAnimationConsumer().run(entity, yaw, tickDelta, matrixStack);
            }
            object.getRenderConsumer().run(vertexConsumerProvider, entity, matrixStack, light, tickDelta);
            if (object.getAnimationConsumer() != null) {
                matrixStack.popPose();
            }
        }

        // render trails
        entity.getTrails().forEach(t -> TrailRenderer.render(t, vertexConsumerProvider, peek));

        matrixStack.popPose();

        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
    }

    protected static void renderObject(Mesh mesh, PoseStack matrixStack, VertexConsumer vertexConsumer, int light) {
        renderObject(mesh, matrixStack, vertexConsumer, light, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    protected static void renderObject(Mesh mesh, PoseStack matrixStack, VertexConsumer vertexConsumer, int light, float r, float g, float b, float a) {
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

    protected static void renderSailObject(Mesh mesh, PoseStack matrixStack, VertexConsumer vertexConsumer, int light, double time) {
        renderSailObject(mesh, matrixStack, vertexConsumer, light, time, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    protected static void renderSailObject(Mesh mesh, PoseStack matrixStack, VertexConsumer vertexConsumer, int light, double time, float r, float g, float b, float a) {
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
                            .color(r, g, b, a).uv(v.t.u, v.t.v)
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(light)
                            .normal(normalMatrix, v.n.x, v.n.y, v.n.z)
                            .endVertex();
                }
            }
        }
    }

    protected static void renderBanner(PoseStack matrixStack, MultiBufferSource vertexConsumers, int light, Mesh mesh, boolean isBanner, List<Pair<Holder<BannerPattern>, DyeColor>> patterns) {
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

    protected static Mesh getFaces(ResourceLocation id, String object) {
        return ObjectLoader.objects.get(id).get(object);
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        if (!entity.shouldRender(x, y, z)) {
            return false;
        }
        AABB box = entity.getBoundingBoxForCulling().inflate(2.5);
        return frustum.isVisible(box);
    }
}

