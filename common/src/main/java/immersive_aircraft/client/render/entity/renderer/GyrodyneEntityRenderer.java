package immersive_aircraft.client.render.entity.renderer;

import com.mojang.math.Axis;
import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.GyrodyneEntity;
import immersive_aircraft.util.Utils;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class GyrodyneEntityRenderer<T extends GyrodyneEntity> extends AircraftEntityRenderer<T> {
    private static final ResourceLocation id = Main.locate("objects/gyrodyne.obj");

    private final ResourceLocation texture;

    private final Model model = new Model()
            .add(
                    new Object(id, "frame")
            )
            .add(
                    new Object(id, "controller").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0, -0.125, 0.84f);
                                matrixStack.mulPose(Axis.ZP.rotationDegrees(-entity.pressingInterpolatedX.getSmooth(tickDelta) * 30.0f));
                                matrixStack.mulPose(Axis.XP.rotationDegrees(entity.pressingInterpolatedZ.getSmooth(tickDelta) * 25.0f));
                                matrixStack.translate(0, 0.125, -0.84f);
                            }
                    )
            )
            .add(
                    new Object(id, "controller_2").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0, -0.125, 0.84f);
                                matrixStack.mulPose(Axis.XP.rotationDegrees(entity.pressingInterpolatedY.getSmooth(tickDelta) * 20.0f));
                                matrixStack.translate(0, 0.125, -0.84f);
                            }
                    )
            )
            .add(
                    new Object(id, "wings").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                float wind = entity.onGround() ? 0.0f : 1.0f;
                                float nx = (float)(Utils.cosNoise((entity.tickCount + tickDelta) / 18.0)) * wind;
                                float ny = (float)(Utils.cosNoise((entity.tickCount + tickDelta) / 19.0)) * wind;

                                matrixStack.mulPose(Axis.XP.rotationDegrees(ny));
                                matrixStack.mulPose(Axis.ZP.rotationDegrees(nx));
                            }
                    )
            )
            .add(
                    new Object(id, "propeller").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(1.0 / 32.0, 0.0, -1.0 / 32.0);
                                matrixStack.mulPose(Axis.YP.rotationDegrees((float)(-entity.engineRotation.getSmooth(tickDelta) * 100.0)));
                                matrixStack.translate(-1.0 / 32.0, 0.0, 1.0 / 32.0);
                            }
                    )
            );

    public GyrodyneEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
        texture = Main.locate("textures/entity/gyrodyne.png");
    }

    @Override
    public ResourceLocation getTextureLocation(@NotNull T aircraft) {
        return texture;
    }

    @Override
    protected Model getModel(AircraftEntity entity) {
        return model;
    }

    @Override
    protected Vector3f getPivot(AircraftEntity entity) {
        return new Vector3f(0.0f, 0.2f, 0.05f);
    }
}
