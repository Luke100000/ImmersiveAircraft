package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.GyrodyneEntity;
import immersive_aircraft.util.Utils;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class GyrodyneEntityRenderer<T extends GyrodyneEntity> extends AircraftEntityRenderer<T> {
    private static final Identifier id = Main.locate("objects/gyrodyne.obj");

    private final Identifier texture;

    private final Model<T> model = new Model<T>()
            .add(
                    new Object<T>(id, "frame")
            )
            .add(
                    new Object<T>(id, "controller").setAnimationConsumer(
                            (object, entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(entity.pressingInterpolatedX.getSmooth(tickDelta)));
                                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.pressingInterpolatedY.getSmooth(tickDelta)));
                            }
                    )
            )
            .add(
                    new Object<T>(id, "controller_2").setAnimationConsumer(
                            (object, entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(entity.pressingInterpolatedZ.getSmooth(tickDelta)));
                            }
                    )
            )
            .add(
                    new Object<T>(id, "wings").setAnimationConsumer(
                            (object, entity, yaw, tickDelta, matrixStack) -> {
                                float WIND = entity.location == AircraftEntity.Location.IN_AIR ? 1.0f : 0.0f;
                                float nx = (float)(Utils.cosNoise((entity.age + tickDelta) / 18.0)) * WIND;
                                float ny = (float)(Utils.cosNoise((entity.age + tickDelta) / 19.0)) * WIND;

                                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(ny));
                                matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(nx));
                            }
                    )
            )
            .add(
                    new Object<T>(id, "propeller").setAnimationConsumer(
                            (object, entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((float)(entity.engineRotation.getSmooth(tickDelta) * 100.0)));
                            }
                    )
            );

    public GyrodyneEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
        texture = Main.locate("textures/entity/gyrodyne.png");
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
        return new Vec3f(0.0f, 0.2f, 0.05f);
    }
}
