package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.AirshipEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public class AirshipEntityRenderer<T extends AirshipEntity> extends AircraftEntityRenderer<T> {
    private static final ResourceLocation ID = Main.locate("airship");

    protected ResourceLocation getModelId() {
        return ID;
    }

    private final ModelPartRenderHandler<T> model = new ModelPartRenderHandler<T>()
            .add("banners", this::renderBanners)
            .add("flag", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) -> renderSails(object, vertexConsumerProvider, entity, matrixStack, light, time))
            .add("flag_small", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) -> renderSails(object, vertexConsumerProvider, entity, matrixStack, light, time))
            .add("flag_front", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) -> renderSails(object, vertexConsumerProvider, entity, matrixStack, light, time));

    public AirshipEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
    }

    @Override
    protected ModelPartRenderHandler<T> getModel(AircraftEntity entity) {
        return model;
    }

    @Override
    protected Vector3f getPivot(AircraftEntity entity) {
        return new Vector3f(0.0f, 0.2f, 0.0f);
    }
}
