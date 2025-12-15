package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.ColorUtils;
import immersive_aircraft.entity.DyeableVehicleEntity;
import immersive_aircraft.resources.bbmodel.BBModel;
import immersive_aircraft.resources.bbmodel.BBObject;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import static immersive_aircraft.client.render.entity.renderer.utils.BBModelRenderer.renderObjectInner;

public abstract class DyeableVehicleEntityRenderer<T extends DyeableVehicleEntity, S extends DyeableVehicleEntityRenderState> extends VehicleEntityRenderer<T, S> {
    public DyeableVehicleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void renderUndyed(BBModel model,
                             BBObject object,
                             PoseStack matrixStack,
                             S entity,
                             SubmitNodeCollector submitNodeCollector) {
        if (entity.dyeColor < 0) {
            renderObjectInner(model, object, matrixStack, entity, submitNodeCollector, null, 1.0f,1.0f,1.0f, 1.0f) ;
        }
    }

    public void renderDyed(BBModel model,
                           BBObject object,
                           PoseStack matrixStack,
                           S entity,
                           SubmitNodeCollector submitNodeCollector,
                           boolean highlight,
                           boolean hideWhenUndyed) {
        if (entity.dyeColor < 0 && hideWhenUndyed) {
            return;
        }
        int color = highlight ? entity.highlightColor : entity.bodyColor;
        float[] rgb = ColorUtils.hexToDecimalRGB(color);
        renderObjectInner(model, object, matrixStack, entity, submitNodeCollector, null,  rgb[0], rgb[1], rgb[2], 1.0f) ;
    }

    @Override
    public void extractRenderState(T entity, S entityRenderState, float f) {
        super.extractRenderState(entity, entityRenderState, f);
        entityRenderState.dyeColor = entity.getDyeColor();
        entityRenderState.bodyColor = entity.getBodyColor();
        entityRenderState.highlightColor = entity.getHighlightColor();
    }
}
