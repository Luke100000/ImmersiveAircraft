package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.BiplaneEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class BiplaneEntityRenderer<T extends BiplaneEntity> extends AircraftEntityRenderer<T> {
    private static final Identifier id = Main.locate("objects/biplane.obj");

    private final Identifier texture;

    private final Model<T> model = new Model<T>()
            .add(
                    new Object<T>(id, "frame")
            )
            .add(
                    new Object<T>(id, "propeller").setAnimationConsumer(
                            (object, entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0.0f, 0.25f, 0.0f);
                                matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((float)(entity.engineRotation.getSmooth(tickDelta) * 100.0)));
                                matrixStack.translate(0.0f, -0.25f, 0.0f);
                            }
                    )
            )
            .add(
                    new Object<T>(id, "elevator").setAnimationConsumer(
                            (object, entity, yaw, tickDelta, matrixStack) -> {
                                Vec3f pivot = object.getPivot();
                                matrixStack.translate(-pivot.getX(), -pivot.getY(), -pivot.getZ());
                                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.pressingInterpolatedY.getSmooth(tickDelta) * 5.0f));
                                matrixStack.translate(pivot.getX(), pivot.getY(), pivot.getZ());
                            }
                    ).setPivot(0.0f, -0.0625f, 2.5f)
            )
            .add(
                    new Object<T>(id, "rudder").setAnimationConsumer(
                            (object, entity, yaw, tickDelta, matrixStack) -> {
                                Vec3f pivot = object.getPivot();
                                matrixStack.translate(-pivot.getX(), -pivot.getY(), -pivot.getZ());
                                matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(entity.pressingInterpolatedX.getSmooth(tickDelta) * 15.0f));
                                matrixStack.translate(pivot.getX(), pivot.getY(), pivot.getZ());
                            }
                    ).setPivot(0.0f, -0.0625f, 2.5f)
            );

    public BiplaneEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
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
    Model<T> getModel(AircraftEntity entity) {
        return model;
    }

    @Override
    Vec3f getPivot(AircraftEntity entity) {
        return new Vec3f(0.0f, 0.4f, 0.05f);
    }
}
