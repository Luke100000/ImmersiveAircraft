package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Config;
import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.AirshipEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import immersive_aircraft.util.obj.oobjloader.Mesh;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

public class AirshipEntityRenderer<T extends AirshipEntity> extends AircraftEntityRenderer<T> {
    private static final Identifier id = Main.locate("objects/airship.obj");

    private final Identifier texture = Main.locate("textures/entity/airship.png");

    private final Model model = new Model()
            .add(
                    new Object(id, "frame")
            )
            .add(
                    new Object(id, "sails")
                            .setRenderConsumer(
                                    (vertexConsumerProvider, entity, matrixStack, light) -> {
                                        Identifier identifier = getTexture(entity);
                                        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(identifier));
                                        if (entity.isWithinParticleRange() && Config.getInstance().enableAnimatedSails) {
                                            Mesh mesh = getFaces(id, "sails_animated");
                                            float time = entity.world.getTime() % 24000 + MinecraftClient.getInstance().getTickDelta();
                                            renderSailObject(mesh, matrixStack, vertexConsumer, light, time);
                                        } else {
                                            Mesh mesh = getFaces(id, "sails");
                                            renderObject(mesh, matrixStack, vertexConsumer, light);
                                        }
                                    }
                            )
            )
            .add(
                    new Object(id, "controller").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.pressingInterpolatedX.getSmooth(tickDelta) * 30.0f));
                                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.pressingInterpolatedZ.getSmooth(tickDelta) * 30.0f));
                            }
                    )
            )
            .add(
                    new Object(id, "propeller")
                            .setAnimationConsumer(
                                    (entity, yaw, tickDelta, matrixStack) -> {
                                        matrixStack.translate(0.0f, 0.1875f, 0.0f);
                                        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(-entity.engineRotation.getSmooth(tickDelta) * 100.0)));
                                        matrixStack.translate(0.0f, -0.1875f, 0.0f);
                                    }
                            )
                            .setRenderConsumer(
                                    (vertexConsumerProvider, entity, matrixStack, light) -> {
                                        Identifier identifier = getTexture(entity);
                                        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(identifier));
                                        Mesh mesh = getFaces(id, "propeller");
                                        renderObject(mesh, matrixStack, vertexConsumer, light);
                                    }
                            )
            );

    public AirshipEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
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
    Vector3f getPivot(AircraftEntity entity) {
        return new Vector3f(0.0f, 0.2f, 0.0f);
    }
}
