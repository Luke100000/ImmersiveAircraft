package immersive_aircraft.client.render.entity.renderer;

import com.mojang.math.Axis;
import immersive_aircraft.Main;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.GyrodyneEntity;
import immersive_aircraft.util.Utils;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class GyrodyneEntityRenderer<T extends GyrodyneEntity> extends AircraftEntityRenderer<T> {
    private static final ResourceLocation ID = Main.locate("gyrodyne");

    protected ResourceLocation getModelId() {
        return ID;
    }

    private final ModelPartRenderHandler<T> model = new ModelPartRenderHandler<T>()
            .add(
                    "wings",
                    (entity, yaw, time, matrixStack) -> {
                        float wind = entity.onGround() ? 0.0f : 1.0f;
                        float nx = (float) (Utils.cosNoise(time / 3.0)) * wind;
                        float ny = (float) (Utils.cosNoise(time / 4.0)) * wind;

                        matrixStack.mulPose(Axis.XP.rotationDegrees(ny));
                        matrixStack.mulPose(Axis.ZP.rotationDegrees(nx));
                    }
            );

    public GyrodyneEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
    }

    @Override
    protected ModelPartRenderHandler<T> getModel(AircraftEntity entity) {
        return model;
    }
}
