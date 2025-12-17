package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.AirshipEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class AirshipEntityRenderer<T extends AirshipEntity> extends AircraftEntityRenderer<T> {
    private static final ResourceLocation ID = Main.locate("airship");

    protected ResourceLocation getModelId() {
        return ID;
    }

    private final ModelPartRenderHandler<AircraftEntityRenderState> model = new ModelPartRenderHandler<AircraftEntityRenderState>()
            .add("banners", this::renderBanners)
            .add("colored", (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                    this.renderDyed(model, object, matrixStack, entity, submitNodeCollector, false, true))
            .add("uncolored", (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                    this.renderUndyed(model, object, matrixStack, entity, submitNodeCollector))
            .add("flag", (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                    renderSails(object, submitNodeCollector, entity, matrixStack))
            .add("flag_small", (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                    renderSails(object, submitNodeCollector, entity, matrixStack))
            .add("flag_front", (model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer) ->
                    renderSails(object, submitNodeCollector, entity, matrixStack));


    public AirshipEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
    }

    @Override
    protected ModelPartRenderHandler<AircraftEntityRenderState> getModel() {
        return model;
    }
}
