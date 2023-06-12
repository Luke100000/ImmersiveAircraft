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

    private final Model model = new Model()
            .add(
                    new Object(id, "frame")
            )
            .add(
                    new Object(id, "controller").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0, -0.125, 0.84f);
                                matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-entity.pressingInterpolatedX.getSmooth(tickDelta) * 30.0f));
                                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.pressingInterpolatedZ.getSmooth(tickDelta) * 25.0f));
                                matrixStack.translate(0, 0.125, -0.84f);
                            }
                    )
            )
            .add(
                    new Object(id, "controller_2").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0, -0.125, 0.84f);
                                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.pressingInterpolatedY.getSmooth(tickDelta) * 20.0f));
                                matrixStack.translate(0, 0.125, -0.84f);
                            }
                    )
            )
            .add(
                    new Object(id, "wings").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                float WIND = entity.isOnGround() ? 0.0f : 1.0f;
                                float nx = (float)(Utils.cosNoise((entity.age + tickDelta) / 18.0)) * WIND;
                                float ny = (float)(Utils.cosNoise((entity.age + tickDelta) / 19.0)) * WIND;

                                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(ny));
                                matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(nx));
                            }
                    )
            )
            .add(
                    new Object(id, "propeller").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(1.0 / 32.0, 0.0, -1.0 / 32.0);
                                matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((float)(-entity.engineRotation.getSmooth(tickDelta) * 100.0)));
                                matrixStack.translate(-1.0 / 32.0, 0.0, 1.0 / 32.0);
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
    protected Model getModel(AircraftEntity entity) {
        return model;
    }

    @Override
    protected Vec3f getPivot(AircraftEntity entity) {
        return new Vec3f(0.0f, 0.2f, 0.05f);
    }
}
