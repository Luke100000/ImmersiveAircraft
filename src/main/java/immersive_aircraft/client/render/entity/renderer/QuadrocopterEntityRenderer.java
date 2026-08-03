package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.QuadrocopterEntity;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class QuadrocopterEntityRenderer<T extends QuadrocopterEntity> extends AircraftEntityRenderer<T> {
    private static final ResourceLocation id = Main.locate("objects/quadrocopter.obj");

    private final ResourceLocation texture = Main.locate("textures/entity/quadrocopter.png");

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
                                    (entity, yaw, tickDelta) -> {
                                        double p = entity.enginePower.getSmooth() / 128.0;
                                        GlStateManager.translate((random.nextDouble() - 0.5) * p, (random.nextDouble() - 0.5) * p, (random.nextDouble() - 0.5) * p);
                                    }
                            )
                            .setRenderConsumer(
                                    (entity, tickDelta) -> {
                                        bindTexture(getTexture(entity));
                                        GlStateManager.disableCull();
                                        Mesh mesh = getFaces(id, "engine_" + (entity.enginePower.getSmooth() > 0.01 ? entity.ticksExisted % 2 : 0));
                                        renderObject(mesh, 1.0f, 1.0f, 1.0f, 1.0f);
                                    }
                            )
            );

    {
        for (float[] propeller : PROPELLERS) {
            model.add(
                    new Object(id, "propeller")
                            .setAnimationConsumer(
                                    (entity, yaw, tickDelta) -> {
                                        GlStateManager.translate(propeller[0], propeller[1], propeller[2]);
                                        GlStateManager.rotate(entity.engineRotation.getSmooth(tickDelta) * propeller[0] * propeller[2] * 200.0f, 0.0f, 1.0f, 0.0f);
                                    }
                            )
                            .setRenderConsumer(
                                    (entity, tickDelta) -> {
                                        bindTexture(getTexture(entity));
                                        GlStateManager.disableCull();
                                        Mesh mesh = getFaces(id, "propeller");
                                        renderObject(mesh, 1.0f, 1.0f, 1.0f, 1.0f);
                                    }
                            )
            );
        }
    }

    public QuadrocopterEntityRenderer(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.8f;
    }

    @Override
    public ResourceLocation getTexture(T entity) {
        return texture;
    }

    @Override
    protected Model getModel(AircraftEntity entity) {
        return model;
    }

    @Override
    protected immersive_aircraft.compat.Vec3f getPivot(AircraftEntity entity) {
        return new immersive_aircraft.compat.Vec3f(0.0f, 0.0f, 0.0f);
    }
}
