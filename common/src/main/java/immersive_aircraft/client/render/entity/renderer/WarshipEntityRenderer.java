package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.WarshipEntity;
import immersive_aircraft.resources.bbmodel.BBMesh;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import static immersive_aircraft.client.render.entity.renderer.utils.BBModelRenderer.renderSailObject;

public class WarshipEntityRenderer<T extends WarshipEntity> extends AirshipEntityRenderer<T> {
    private static final ResourceLocation ID = Main.locate("warship");

    private final ModelPartRenderHandler<T> model = new ModelPartRenderHandler<T>()
            .add("left_balloon_colored", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) ->
                    renderDyed(model, object, vertexConsumerProvider, entity, matrixStack, light, time, false, true))
            .add("left_balloon_uncolored", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) ->
                    renderUndyed(model, object, vertexConsumerProvider, entity, matrixStack, light, time))
            .add("right_balloon_colored", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) ->
                    renderDyed(model, object, vertexConsumerProvider, entity, matrixStack, light, time, false, true))
            .add("right_balloon_uncolored", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) ->
                    renderUndyed(model, object, vertexConsumerProvider, entity, matrixStack, light, time))
            .add("centre_balloon_colored", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) ->
                    renderDyed(model, object, vertexConsumerProvider, entity, matrixStack, light, time, false, true))
            .add("centre_balloon_uncolored", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) ->
                    renderUndyed(model, object, vertexConsumerProvider, entity, matrixStack, light, time))
            .add("tail_fin_flag", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) -> renderSails(object, vertexConsumerProvider, entity, matrixStack, light, time))
            .add("nose_fin_top_flag", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) -> renderSails(object, vertexConsumerProvider, entity, matrixStack, light, time))
            .add("nose_fin_bottom_flag", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) -> renderSails(object, vertexConsumerProvider, entity, matrixStack, light, time))
            .add("net", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) -> renderSailObject((BBMesh) object, matrixStack, vertexConsumerProvider, light, time, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, (float) (0.005f + entity.getSpeedVector().length() * 0.05f)));

    @Override
    protected ModelPartRenderHandler<T> getModel(AircraftEntity entity) {
        return model;
    }

    protected ResourceLocation getModelId() {
        return ID;
    }

    public WarshipEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 2.5f;
    }

    @Override
    protected double getCullingBoundingBoxInflation() {
        return 5.0;
    }
}
