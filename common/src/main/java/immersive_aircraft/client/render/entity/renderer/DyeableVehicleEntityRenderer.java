package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.ColorUtils;
import immersive_aircraft.entity.DyeableVehicleEntity;
import immersive_aircraft.resources.bbmodel.BBModel;
import immersive_aircraft.resources.bbmodel.BBObject;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import static immersive_aircraft.client.render.entity.renderer.utils.BBModelRenderer.renderObjectInner;

public abstract class DyeableVehicleEntityRenderer<T extends DyeableVehicleEntity> extends VehicleEntityRenderer<T> {
    public DyeableVehicleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void renderUndyed(BBModel model, BBObject object, MultiBufferSource vertexConsumerProvider, T entity, PoseStack matrixStack, int light, float time) {
        if (entity.getDyeColor() < 0) {
            renderObjectInner(model, object, matrixStack, vertexConsumerProvider, light, time, entity,  null,  1.0f,1.0f,1.0f, 1.0f) ;
        }
    }

    public void renderDyed(BBModel model, BBObject object, MultiBufferSource vertexConsumerProvider, T entity, PoseStack matrixStack, int light, float time, boolean highlight, boolean hideWhenUndyed) {
        if (entity.getDyeColor() < 0 && hideWhenUndyed) {
            return;
        }
        int color = highlight ? entity.getHighlightColor() : entity.getBodyColor();
        float[] rgb = ColorUtils.hexToDecimalRGB(color);
        renderObjectInner(model, object, matrixStack, vertexConsumerProvider, light, time, entity,  null,  rgb[0], rgb[1], rgb[2], 1.0f) ;
    }
}
