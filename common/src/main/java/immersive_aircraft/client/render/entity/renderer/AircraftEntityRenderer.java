package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.resources.ObjectLoader;
import immersive_aircraft.util.Utils;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import owens.oobjloader.Face;
import owens.oobjloader.FaceVertex;
import owens.oobjloader.Mesh;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public abstract class AircraftEntityRenderer<T extends AircraftEntity> extends EntityRenderer<T> {
    static class Object<T extends AircraftEntity> {
        public interface AnimationConsumer<T extends AircraftEntity> {
            void run(Object<T> object, T entity, float yaw, float tickDelta, MatrixStack matrixStack);
        }

        public interface RenderConsumer<T extends AircraftEntity> {
            void run(VertexConsumer vertexConsumer, T entity, MatrixStack matrixStack, int light);
        }

        Object(Identifier id, String object) {
            this.id = id;
            this.object = object;
        }

        private final Identifier id;
        private final String object;
        private Vec3f pivot = new Vec3f();

        private AnimationConsumer<T> animationConsumer = null;
        private RenderConsumer<T> renderConsumer = (vertexConsumer, entity, matrixStack, light) -> renderObject(getMesh(), matrixStack, vertexConsumer, light);

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

        public String getObject() {
            return object;
        }

        public Vec3f getPivot() {
            return pivot;
        }

        public Object<T> setPivot(float x, float y, float z) {
            this.pivot = new Vec3f(x, y, z);
            return this;
        }

        @Nullable
        public AnimationConsumer<T> getAnimationConsumer() {
            return animationConsumer;
        }

        public Object<T> setAnimationConsumer(AnimationConsumer<T> animationConsumer) {
            this.animationConsumer = animationConsumer;
            return this;
        }

        public RenderConsumer<T> getRenderConsumer() {
            return renderConsumer;
        }

        public Object<T> setRenderConsumer(RenderConsumer<T> renderConsumer) {
            this.renderConsumer = renderConsumer;
            return this;
        }
    }

    static class Model<T extends AircraftEntity> {
        private final List<Object<T>> objects = new LinkedList<>();

        public Model<T> add(Object<T> o) {
            objects.add(o);
            return this;
        }

        public List<Object<T>> getObjects() {
            return objects;
        }
    }

    public AircraftEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    abstract Model<T> getModel(AircraftEntity entity);

    abstract Vec3f getPivot(AircraftEntity entity);


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

        float WIND = entity.location == AircraftEntity.Location.IN_AIR ? entity.getProperties().getWindSensitivity() * 10.0f : 0.0f;
        float nx = (float)(Utils.cosNoise((entity.age + tickDelta) / 20.0)) * WIND;
        float ny = (float)(Utils.cosNoise((entity.age + tickDelta) / 21.0)) * WIND;

        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-yaw));
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.getPitch(tickDelta) + ny));
        matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(entity.getRoll(tickDelta) + nx));

        Vec3f pivot = getPivot(entity);
        matrixStack.translate(pivot.getX(), pivot.getY(), pivot.getZ());

        //Get vertex consumer
        Identifier identifier = getTexture(entity);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutout(identifier));

        //Render parts
        Model<T> model = getModel(entity);
        for (Object<T> object : model.getObjects()) {
            if (object.getAnimationConsumer() != null) {
                matrixStack.push();
                object.getAnimationConsumer().run(object, entity, yaw, tickDelta, matrixStack);
            }
            object.getRenderConsumer().run(vertexConsumer, entity, matrixStack, light);
            if (object.getAnimationConsumer() != null) {
                matrixStack.pop();
            }
        }

        //Todo move that into custom renderer
        //List<Pair<BannerPattern, DyeColor>> list = new LinkedList<>();
        //list.add(new Pair<>(BannerPattern.CREEPER, DyeColor.RED));
        //BannerBlockEntityRenderer.renderCanvas(matrixStack, vertexConsumerProvider, light, OverlayTexture.DEFAULT_UV, part, ModelLoader.BANNER_BASE, false, list)

        // render trails
        entity.getTrails().forEach(t -> t.render(vertexConsumerProvider, peek));

        matrixStack.pop();

        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
    }

    static void renderObject(Mesh mesh, MatrixStack matrixStack, VertexConsumer vertexConsumer, int light) {
        for (Face face : mesh.faces) {
            if (face.vertices.size() == 4) {
                for (FaceVertex v : face.vertices) {
                    vertex(matrixStack, vertexConsumer, v.v.x, v.v.y, v.v.z, v.t.u, v.t.v, v.n.x, v.n.y, v.n.z, light);
                }
            }
        }
    }

    static void renderSailObject(Mesh mesh, MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, double time) {
        for (Face face : mesh.faces) {
            if (face.vertices.size() == 4) {
                for (FaceVertex v : face.vertices) {
                    double angle = v.v.x + v.v.z + v.v.y * 0.25 + time * 0.25;
                    double scale = 0.05;
                    vertex(matrixStack, vertexConsumer, (float)(v.v.x + Math.cos(angle) * scale + Math.cos(angle * 1.7) * scale), v.v.y, (float)(v.v.z + Math.sin(angle) * scale + Math.sin(angle * 1.7) * scale), v.t.u, v.t.v, v.n.x, v.n.y, v.n.z, light);
                }
            }
        }
    }

    static Mesh getFaces(Identifier id, String object) {
        return ObjectLoader.objects.get(id).get(object);
    }

    private static void vertex(MatrixStack matrices, VertexConsumer vertexConsumer, float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ, int light) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();
        Matrix3f normalMatrix = entry.getNormalMatrix();
        vertexConsumer.vertex(positionMatrix, x, y, z).color(255, 255, 255, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, normalX, normalY, normalZ).next();
    }
}

