package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.entity.DyeableVehicleEntity;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.client.renderer.entity.RenderManager;

/**
 * Base for renderers of dyeable vehicles, offering the 1.20.1 renderUndyed/renderDyed
 * helpers (undyed parts render only without a dye color, dyed parts tinted with the
 * entity's body/highlight color).
 */
public abstract class DyeableVehicleEntityRenderer<T extends DyeableVehicleEntity> extends AircraftEntityRenderer<T> {
    public DyeableVehicleEntityRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    public void renderUndyed(T entity, Mesh mesh) {
        if (entity.getDyeColor() < 0) {
            renderObject(mesh, 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    public void renderDyed(T entity, Mesh mesh, boolean highlight, boolean hideWhenUndyed) {
        if (entity.getDyeColor() < 0 && hideWhenUndyed) {
            return;
        }
        int color = highlight ? entity.getHighlightColor() : entity.getBodyColor();
        float[] rgb = DyeableVehicleEntity.hexToDecimalRGB(color);
        renderObject(mesh, rgb[0], rgb[1], rgb[2], 1.0f);
    }
}
