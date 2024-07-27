package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.ColorUtils;
import immersive_aircraft.client.render.entity.renderer.utils.BBModelRenderer;
import immersive_aircraft.entity.DyeableVehicleEntity;
import immersive_aircraft.resources.bbmodel.BBFaceContainer;
import immersive_aircraft.resources.bbmodel.BBObject;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public abstract class DyeableVehicleEntityRenderer<T extends DyeableVehicleEntity> extends VehicleEntityRenderer<T> {
    public DyeableVehicleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void renderUndyed(BBObject object, MultiBufferSource vertexConsumerProvider, T entity, PoseStack matrixStack, int light) {
        if (entity.getDyeColor() > 0 && object instanceof BBFaceContainer faces) {
            BBModelRenderer.renderFaces(faces, matrixStack, vertexConsumerProvider, light,
                    1.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 0.0f, 0.0f, null);
        }
    }

    public void renderDyed(BBObject object, MultiBufferSource vertexConsumerProvider, T entity, PoseStack matrixStack, int light, boolean highlight, boolean hideWhenUndyed) {
        if (entity.getDyeColor() == 0 && hideWhenUndyed) {
            return;
        }
        int color = highlight ? entity.getHighlightColor() : entity.getBodyColor();
        float[] rgb = ColorUtils.hexToDecimalRGB(color);
        if (object instanceof BBFaceContainer faces) {
            BBModelRenderer.renderFaces(faces, matrixStack, vertexConsumerProvider, light,
                    rgb[0], rgb[1], rgb[2], 1.0f,
                    1.0f, 1.0f, 0.0f, 0.0f, null);
        }
    }
}
