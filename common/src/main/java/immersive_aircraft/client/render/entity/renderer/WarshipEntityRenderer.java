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

    private final ModelPartRenderHandler<AircraftEntityRenderState> model = new ModelPartRenderHandler<AircraftEntityRenderState>()
            .add("left_balloon_colored",
                    (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                            renderDyed(model, object, matrixStack, entity, submitNodeCollector, false, true))
            .add("left_balloon_uncolored",
                    (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                            renderUndyed(model, object, matrixStack, entity, submitNodeCollector))
            .add("right_balloon_colored",
                    (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                            renderDyed(model, object, matrixStack, entity, submitNodeCollector, false, true))
            .add("right_balloon_uncolored",
                    (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                            renderUndyed(model, object, matrixStack, entity, submitNodeCollector))
            .add("centre_balloon_colored",
                    (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                            renderDyed(model, object, matrixStack, entity, submitNodeCollector, false, true))
            .add("centre_balloon_uncolored",
                    (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                            renderUndyed(model, object, matrixStack, entity, submitNodeCollector))
            .add("tail_fin_flag",
                    (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                            renderSails(object, submitNodeCollector, entity, matrixStack))
            .add("nose_fin_top_flag",
                    (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                            renderSails(object, submitNodeCollector, entity, matrixStack))
            .add("nose_fin_bottom_flag",
                    (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                            renderSails(object, submitNodeCollector, entity, matrixStack))
            .add("net",
                    (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                            renderSailObject((BBMesh) object, matrixStack, submitNodeCollector, entity.packedLight, entity.time, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, (float) (0.005f + entity.speedVector.length() * 0.05f)));

    @Override
    protected ModelPartRenderHandler<AircraftEntityRenderState> getModel() {
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
