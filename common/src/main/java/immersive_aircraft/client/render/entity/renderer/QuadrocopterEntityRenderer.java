package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.QuadrocopterEntity;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

import java.util.Random;

public class QuadrocopterEntityRenderer<T extends QuadrocopterEntity> extends AircraftEntityRenderer<T> {
    private static final Identifier id = Main.locate("objects/quadrocopter.obj");

    private final Identifier texture = Main.locate("textures/entity/quadrocopter.png");

    private final Random random = new Random();

    private static final float[][] PROPELLERS = {
            {1.25f, 0.5f + 0.5f / 16.0f, 0.65625f},
            {-1.25f, 0.5f + 0.5f / 16.0f, 0.65625f},
            {1.25f, 0.5f + 0.5f / 16.0f, -0.71875f},
            {-1.25f, 0.5f + 0.5f / 16.0f, -0.71875f}
    };

    private final Model model = new Model()
            .add(
                    new Object(id, "frame")
            ).add(
                    new Object(id, "engine")
                            .setAnimationConsumer(
                                    (entity, yaw, tickDelta, matrixStack) -> {
                                        double p = entity.enginePower.getSmooth() / 128.0;
                                        matrixStack.translate((random.nextDouble() - 0.5) * p, (random.nextDouble() - 0.5) * p, (random.nextDouble() - 0.5) * p);
                                    }
                            )
                            .setRenderConsumer(
                                    (vertexConsumerProvider, entity, matrixStack, light, tickDelta) -> {
                                        Identifier identifier = getTexture(entity);
                                        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(identifier));
                                        Mesh mesh = getFaces(id, "engine_" + (entity.enginePower.getSmooth() > 0.01 ? entity.age % 2 : 0));
                                        renderObject(mesh, matrixStack, vertexConsumer, light);
                                    }
                            )
            );

    {
        for (float[] propeller : PROPELLERS) {
            model.add(
                    new Object(id, "propeller")
                            .setAnimationConsumer(
                                    (entity, yaw, tickDelta, matrixStack) -> {
                                        matrixStack.translate(propeller[0], propeller[1], propeller[2]);
                                        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.engineRotation.getSmooth(tickDelta) * propeller[0] * propeller[2] * 200.0f));
                                    }
                            )
                            .setRenderConsumer(
                                    (vertexConsumerProvider, entity, matrixStack, light, tickDelta) -> {
                                        Identifier identifier = getTexture(entity);
                                        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(identifier));
                                        Mesh mesh = getFaces(id, "propeller");
                                        renderObject(mesh, matrixStack, vertexConsumer, light);
                                    }
                            )
            );
        }
    }

    public QuadrocopterEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
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
    protected Vector3f getPivot(AircraftEntity entity) {
        return new Vector3f(0.0f, 0.0f, 0.0f);
    }
}
