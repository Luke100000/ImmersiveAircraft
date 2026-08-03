package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.GyrodyneEntity;
import immersive_aircraft.util.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class GyrodyneEntityRenderer<T extends GyrodyneEntity> extends AircraftEntityRenderer<T> {
    private static final ResourceLocation id = Main.locate("objects/gyrodyne.obj");

    private final ResourceLocation texture;

    private final Model model = new Model()
            .add(
                    new Object(id, "frame")
            )
            .add(
                    new Object(id, "controller").setAnimationConsumer(
                            (entity, yaw, tickDelta) -> {
                                GlStateManager.translate(0, -0.125, 0.84f);
                                GlStateManager.rotate(-entity.pressingInterpolatedX.getSmooth(tickDelta) * 30.0f, 0.0f, 0.0f, 1.0f);
                                GlStateManager.rotate(entity.pressingInterpolatedZ.getSmooth(tickDelta) * 25.0f, 1.0f, 0.0f, 0.0f);
                                GlStateManager.translate(0, 0.125, -0.84f);
                            }
                    )
            )
            .add(
                    new Object(id, "controller_2").setAnimationConsumer(
                            (entity, yaw, tickDelta) -> {
                                GlStateManager.translate(0, -0.125, 0.84f);
                                GlStateManager.rotate(entity.pressingInterpolatedY.getSmooth(tickDelta) * 20.0f, 1.0f, 0.0f, 0.0f);
                                GlStateManager.translate(0, 0.125, -0.84f);
                            }
                    )
            )
            .add(
                    new Object(id, "wings").setAnimationConsumer(
                            (entity, yaw, tickDelta) -> {
                                float WIND = entity.onGround ? 0.0f : 1.0f;
                                float nx = (float)(Utils.cosNoise((entity.ticksExisted + tickDelta) / 18.0)) * WIND;
                                float ny = (float)(Utils.cosNoise((entity.ticksExisted + tickDelta) / 19.0)) * WIND;

                                GlStateManager.rotate(ny, 1.0f, 0.0f, 0.0f);
                                GlStateManager.rotate(nx, 0.0f, 0.0f, 1.0f);
                            }
                    )
            )
            .add(
                    new Object(id, "propeller").setAnimationConsumer(
                            (entity, yaw, tickDelta) -> {
                                GlStateManager.translate(1.0 / 32.0, 0.0, -1.0 / 32.0);
                                GlStateManager.rotate((float)(-entity.engineRotation.getSmooth(tickDelta) * 100.0), 0.0f, 1.0f, 0.0f);
                                GlStateManager.translate(-1.0 / 32.0, 0.0, 1.0 / 32.0);
                            }
                    )
            );

    public GyrodyneEntityRenderer(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.8f;
        texture = Main.locate("textures/entity/gyrodyne.png");
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
        return new immersive_aircraft.compat.Vec3f(0.0f, 0.2f, 0.05f);
    }
}
