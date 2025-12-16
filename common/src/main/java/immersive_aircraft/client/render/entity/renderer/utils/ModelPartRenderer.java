package immersive_aircraft.client.render.entity.renderer.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.render.entity.renderer.VehicleEntityRenderState;
import immersive_aircraft.resources.bbmodel.BBModel;
import immersive_aircraft.resources.bbmodel.BBObject;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

public record ModelPartRenderer<T extends EntityRenderState>(
        String id,
        ModelPartRenderer.AnimationConsumer<T> animationConsumer,
        ModelPartRenderer.RenderConsumer<T> renderConsumer
) {
    public interface AnimationConsumer<T> {
        void run(T entity, PoseStack matrixStack, float time);
    }

    public interface RenderConsumer<T extends EntityRenderState> {
        void run(BBModel model,
                 BBObject object,
                 SubmitNodeCollector submitNodeCollector,
                 T entity,
                 PoseStack matrixStack,
                 ModelPartRenderHandler<T> modelPartRenderer);
    }
}
