package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.QuadrocopterEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

public class QuadrocopterEntityRenderer<T extends QuadrocopterEntity> extends AircraftEntityRenderer<T> {
    private static final ResourceLocation ID = Main.locate("quadrocopter");

    protected ResourceLocation getModelId() {
        return ID;
    }

    private final Random random = new Random();

    private final ModelPartRenderHandler<AircraftEntityRenderState> model = new ModelPartRenderHandler<AircraftEntityRenderState>()
            .add(
                    "engine",
                    (entity, poseStack, tickDelta) -> {
                       double p = entity.enginePower / 128.0;
                       poseStack.translate((random.nextDouble() - 0.5) * p, (random.nextDouble() - 0.5) * p, (random.nextDouble() - 0.5) * p);
                    },
                    (model, object, vertexConsumerProvider, entity, matrixStack, modelPartRenderer) -> {
                        String engine = "engine_" + (entity.enginePower > 0.01 ? entity.tickCount % 2 : 0);
                        renderOptionalObject(engine, model, matrixStack, entity, vertexConsumerProvider);
                    }
            );

    public QuadrocopterEntityRenderer(EntityRendererProvider.Context context) {
        super(context);

        this.shadowRadius = 0.8f;
    }

    @Override
    protected ModelPartRenderHandler<AircraftEntityRenderState> getModel() {
        return model;
    }
}
