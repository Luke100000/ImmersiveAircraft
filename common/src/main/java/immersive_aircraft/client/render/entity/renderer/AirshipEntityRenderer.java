package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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

    private final ModelPartRenderHandler<T> model = new ModelPartRenderHandler<T>()
            .add("banners", this::renderBanners)
            .add("colored", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) ->
                    renderDyed(model, object, vertexConsumerProvider, entity, matrixStack, light, time, false, true))
            .add("uncolored", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) ->
                    renderUndyed(model, object, vertexConsumerProvider, entity, matrixStack, light, time))
            .add("flag", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) ->
                    renderSails(object, vertexConsumerProvider, entity, matrixStack, light, time))
            .add("flag_small", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) ->
                    renderSails(object, vertexConsumerProvider, entity, matrixStack, light, time))
            .add("flag_front", (model, object, vertexConsumerProvider, entity, matrixStack, light, time, modelPartRenderer) ->
                    renderSails(object, vertexConsumerProvider, entity, matrixStack, light, time))
            .add("front_left_pod", this::animateHoverPod)
            .add("front_right_pod", this::animateHoverPod)
            .add("rear_left_pod", this::animateHoverPod)
            .add("rear_right_pod", this::animateHoverPod);


    public AirshipEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
    }

    private void animateHoverPod(T entity, float yaw, float time, PoseStack matrixStack) {
        float tilt = entity.pressingInterpolatedZ.getSmooth(Main.frameTime) * 25.0f;
        matrixStack.mulPose(Axis.XP.rotationDegrees(tilt));
    }

    @Override
    protected ModelPartRenderHandler<T> getModel(AircraftEntity entity) {
        return model;
    }
}
