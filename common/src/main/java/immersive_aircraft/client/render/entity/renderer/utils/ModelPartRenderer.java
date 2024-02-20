package immersive_aircraft.client.render.entity.renderer.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.resources.bbmodel.BBModel;
import immersive_aircraft.resources.bbmodel.BBObject;
import net.minecraft.client.renderer.MultiBufferSource;

public record ModelPartRenderer<T extends AircraftEntity>(
        String id,
        ModelPartRenderer.AnimationConsumer<T> animationConsumer,
        ModelPartRenderer.RenderConsumer<T> renderConsumer
) {
    public interface AnimationConsumer<T> {
        void run(T entity, float yaw, float time, PoseStack matrixStack);
    }

    public interface RenderConsumer<T extends AircraftEntity> {
        void run(BBModel model, BBObject object, MultiBufferSource vertexConsumerProvider, T entity, PoseStack matrixStack, int light, float time, ModelPartRenderHandler<T> modelPartRenderer);
    }
}
