package immersive_aircraft.client.render.entity.renderer;

import com.mojang.datafixers.util.Pair;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.resources.ObjectLoader;
import immersive_aircraft.util.obj.Face;
import immersive_aircraft.util.obj.FaceVertex;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.RegistryEntry;

import java.util.LinkedList;
import java.util.List;

public abstract class AircraftEntityRenderer<T extends AircraftEntity> extends EntityRenderer<T> {
	protected class Object {
        public interface AnimationConsumer<T> {
            void run(T entity, float yaw, float tickDelta, MatrixStack matrixStack);
        }

        public interface RenderConsumer<T> {
            void run(VertexConsumerProvider vertexConsumerProvider, T entity, MatrixStack matrixStack, int light, float tickDelta);
        }

        public Object(Identifier id, String object) {
            this.id = id;
            this.object = object;
        }

        private final Identifier id;
        private final String object;

        private AnimationConsumer<T> animationConsumer = null;
        private RenderConsumer<T> renderConsumer = (vertexConsumerProvider, entity, matrixStack, light, tickDelta) -> {
            //Get vertex consumer
            Identifier identifier = getTexture(entity);
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutout(identifier));
            renderObject(getMesh(), matrixStack, vertexConsumer, light);
        };

        public Mesh getMesh() {
            Mesh mesh = getFaces(id, object);
            if (mesh == null) {
                throw new RuntimeException(String.format("Mesh %s in %s does not exist!", id, object));
            }
            return mesh;
        }

        public Identifier getId() {
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
		
        private final List<Object> objects = new LinkedList<>();

        public Model add(Object o) {
            objects.add(o);
            return this;
        }

        public List<Object> getObjects() {
            return objects;
        }
    }

    public AircraftEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    protected abstract Model getModel(AircraftEntity entity);

    protected abstract Vec3f getPivot(AircraftEntity entity);


    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
        MatrixStack.Entry peek = matrixStack.peek();

        matrixStack.push();

        //Wobble
        float h = (float)entity.getDamageWobbleTicks() - tickDelta;
        float j = entity.getDamageWobbleStrength() - tickDelta;
        if (j < 0.0f) {
            j = 0.0f;
        }
        if (h > 0.0f) {
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(MathHelper.sin(h) * h * j / 10.0f * (float)entity.getDamageWobbleSide()));
        }

        Vec3f effect = entity.isOnGround() ? Vec3f.ZERO : entity.getWindEffect();
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-yaw));
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.getPitch(tickDelta) + effect.getZ()));
        matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(entity.getRoll(tickDelta) + effect.getX()));

        Vec3f pivot = getPivot(entity);
        matrixStack.translate(pivot.getX(), pivot.getY(), pivot.getZ());

        //Render parts
        Model model = getModel(entity);
        for (Object object : model.getObjects()) {
            if (object.getAnimationConsumer() != null) {
                matrixStack.push();
                object.getAnimationConsumer().run(entity, yaw, tickDelta, matrixStack);
            }
            object.getRenderConsumer().run(vertexConsumerProvider, entity, matrixStack, light, tickDelta);
            if (object.getAnimationConsumer() != null) {
                matrixStack.pop();
            }
        }

        // render trails
        entity.getTrails().forEach(t -> TrailRenderer.render(t, vertexConsumerProvider, peek));

        matrixStack.pop();

        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
    }

    protected static void renderObject(Mesh mesh, MatrixStack matrixStack, VertexConsumer vertexConsumer, int light) {
        renderObject(mesh, matrixStack, vertexConsumer, light, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    protected static void renderObject(Mesh mesh, MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, float r, float g, float b, float a) {
        MatrixStack.Entry entry = matrixStack.peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();
        Matrix3f normalMatrix = entry.getNormalMatrix();
        for (Face face : mesh.faces) {
            if (face.vertices.size() == 4) {
                for (FaceVertex v : face.vertices) {
                    vertexConsumer
                            .vertex(positionMatrix, v.v.x, v.v.y, v.v.z)
                            .color(r, g, b, a)
                            .texture(v.t.u, v.t.v)
                            .overlay(OverlayTexture.DEFAULT_UV)
                            .light(light)
                            .normal(normalMatrix, v.n.x, v.n.y, v.n.z)
                            .next();
                }
            }
        }
    }

    protected static void renderSailObject(Mesh mesh, MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, double time) {
        renderSailObject(mesh, matrixStack, vertexConsumer, light, time, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    protected static void renderSailObject(Mesh mesh, MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, double time, float r, float g, float b, float a) {
        MatrixStack.Entry entry = matrixStack.peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();
        Matrix3f normalMatrix = entry.getNormalMatrix();
        for (Face face : mesh.faces) {
            if (face.vertices.size() == 4) {
                for (FaceVertex v : face.vertices) {
                    double angle = v.v.x + v.v.z + v.v.y * 0.25 + time * 0.25;
                    double scale = 0.05;
                    float x = (float)(v.v.x + (Math.cos(angle) + Math.cos(angle * 1.7)) * scale * v.c.r);
                    float z = (float)(v.v.z + (Math.sin(angle) + Math.sin(angle * 1.7)) * scale * v.c.r);
                    vertexConsumer
                            .vertex(positionMatrix, x, v.v.y, z)
                            .color(r, g, b, a).texture(v.t.u, v.t.v)
                            .overlay(OverlayTexture.DEFAULT_UV)
                            .light(light)
                            .normal(normalMatrix, v.n.x, v.n.y, v.n.z)
                            .next();
                }
            }
        }
    }

    protected static void renderBanner(MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, Mesh mesh, boolean isBanner, List<Pair<RegistryEntry<BannerPattern>, DyeColor>> patterns) {
        for (int i = 0; i < 17 && i < patterns.size(); ++i) {
            Pair<RegistryEntry<BannerPattern>, DyeColor> pair = patterns.get(i);
            float[] fs = pair.getSecond().getColorComponents();
            RegistryEntry<BannerPattern> bannerPattern = pair.getFirst();
            bannerPattern.getKey().ifPresent(key -> {
                SpriteIdentifier spriteIdentifier = isBanner ? TexturedRenderLayers.getBannerPatternTextureId(key) : TexturedRenderLayers.getShieldPatternTextureId(key);
                VertexConsumer vertexConsumer = spriteIdentifier.getVertexConsumer(vertexConsumers, RenderLayer::getEntityNoOutline);
                Sprite sprite = spriteIdentifier.getSprite();
                MatrixStack.Entry entry = matrixStack.peek();
                Matrix4f positionMatrix = entry.getPositionMatrix();
                Matrix3f normalMatrix = entry.getNormalMatrix();
                for (Face face : mesh.faces) {
                    if (face.vertices.size() == 4) {
                        for (FaceVertex v : face.vertices) {
                            vertexConsumer
                                    .vertex(positionMatrix, v.v.x, v.v.y, v.v.z)
                                    .color(fs[0], fs[1], fs[2], 1.0f)
                                    .texture(v.t.u * (sprite.getMaxU() - sprite.getMinU()) + sprite.getMinU(), v.t.v * (sprite.getMaxV() - sprite.getMinV()) + sprite.getMinV())
                                    .overlay(OverlayTexture.DEFAULT_UV)
                                    .light(light)
                                    .normal(normalMatrix, v.n.x, v.n.y, v.n.z)
                                    .next();
                        }
                    }
                }
            });
        }
    }

    protected static Mesh getFaces(Identifier id, String object) {
        return ObjectLoader.objects.get(id).get(object);
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        if (!entity.shouldRender(x, y, z)) {
            return false;
        }
        Box box = entity.getVisibilityBoundingBox().expand(2.5);
        return frustum.isVisible(box);
    }
}

