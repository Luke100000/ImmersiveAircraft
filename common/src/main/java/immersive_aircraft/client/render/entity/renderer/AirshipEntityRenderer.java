package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.AirshipEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class AirshipEntityRenderer<T extends AirshipEntity> extends AircraftEntityRenderer<T> {
    private static final Identifier id = Main.locate("objects/airship.obj");

    private final Identifier texture;

    private final Model<T> model = new Model<T>()
            .add(
                    new Object<T>(id, "frame")
            )
            .add(
                    new Object<T>(id, "sails")
            )
            /*
            .add(
                    new Object<T>(id, "sail")
                            .setRenderConsumer(
                                    (vertexConsumer, entity, matrixStack, light) -> {
                                        Mesh mesh = getFaces(id, "sails");
                                        renderSailObject(mesh, matrixStack, vertexConsumer, light, entity.world.getTime() % 24000 + MinecraftClient.getInstance().getTickDelta());
                                    }
                            )
            )
             */
            .add(
                    new Object<T>(id, "controller").setAnimationConsumer(
                            (object, entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(entity.pressingInterpolatedX.getSmooth(tickDelta)));
                                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.pressingInterpolatedY.getSmooth(tickDelta)));
                            }
                    )
            )
            .add(
                    new Object<T>(id, "propeller").setAnimationConsumer(
                            (object, entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0.0f, 0.275f, 0.0f);
                                matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((float)(entity.engineRotation.getSmooth(tickDelta) * 100.0)));
                                matrixStack.translate(0.0f, -0.275f, 0.0f);
                            }
                    )
            );

    public AirshipEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.8f;

        texture = Main.locate("textures/entity/airship.png");
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
        return new Vec3f(0.0f, 0.2f, 0.0f);
    }
}
