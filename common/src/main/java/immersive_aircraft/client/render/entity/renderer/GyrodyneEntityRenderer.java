package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.client.render.entity.model.GyrodyneEntityModel;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.GyrodyneEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class GyrodyneEntityRenderer<T extends GyrodyneEntity> extends AircraftEntityRenderer<T> {
    private static final Identifier id = Main.locate("objects/gyrodyne.obj");

    private final Identifier texture;

    private final Model<T> model = new Model<T>()
            .add(
                    new Object<T>(id, "left")
            )
            .add(
                    new Object<T>(id, "left").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(entity.getEnginePower() * (entity.age + tickDelta)));
                            }
                    )
            );

    public GyrodyneEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
        texture = Main.locate("textures/entity/aircraft.png");
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
        return new Vec3f(0.0f, 0.5f, 0.05f);
    }
}
