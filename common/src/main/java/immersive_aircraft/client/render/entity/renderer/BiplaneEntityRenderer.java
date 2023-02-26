package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.BiplaneEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class BiplaneEntityRenderer<T extends BiplaneEntity> extends AircraftEntityRenderer<T> {
    private static final Identifier id = Main.locate("objects/biplane.obj");

    private final Identifier texture;

    private final Model model = new Model()
            .add(
                    new Object(id, "frame")
            )
            /*
            .add(
                    new Object(id, "banners").setRenderConsumer(
                            (vertexConsumerProvider, entity, matrixStack, light) -> {
                                List<Pair<BannerPattern, DyeColor>> patterns = new LinkedList<>();
                                patterns.add(new Pair<>(BannerPattern.CREEPER, DyeColor.RED));
                                Mesh mesh = getFaces(id, "banners");
                                renderBanner(matrixStack, vertexConsumerProvider, light, mesh, true, patterns);
                            }
                    )
            )
             */
            .add(
                    new Object(id, "propeller").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0.0f, 0.3125f, 0.0f);
                                matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((float)(entity.engineRotation.getSmooth(tickDelta) * 100.0)));
                                matrixStack.translate(0.0f, -0.3125f, 0.0f);
                            }
                    )
            )
            .add(
                    new Object(id, "elevator").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0.0f, 0.0625f, -2.5f);
                                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-entity.pressingInterpolatedZ.getSmooth(tickDelta) * 20.0f));
                                matrixStack.translate(0.0f, -0.0625f, 2.5f);
                            }
                    )
            )
            .add(
                    new Object(id, "rudder").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0.0f, 0.0625f, -2.5f);
                                matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(entity.pressingInterpolatedX.getSmooth(tickDelta) * 18.0f));
                                matrixStack.translate(0.0f, -0.0625f, 2.5f);
                            }
                    )
            );

    public BiplaneEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
        this.shadowRadius = 0.8f;
        texture = Main.locate("textures/entity/biplane.png");
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(T AircraftEntity) {
        return texture;
    }

    @Override
    Model getModel(AircraftEntity entity) {
        return model;
    }

    @Override
    Vec3f getPivot(AircraftEntity entity) {
        return new Vec3f(0.0f, 0.4f, 0.05f);
    }
}
