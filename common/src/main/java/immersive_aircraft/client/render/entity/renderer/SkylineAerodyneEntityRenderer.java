package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.SkylineAerodyneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Renderer for the original aerodyne model generated from the Img2Blockbench source spec. */
public class SkylineAerodyneEntityRenderer<T extends SkylineAerodyneEntity> extends AircraftEntityRenderer<T> {
    private static final ResourceLocation ID = Main.locate("skyline_aerodyne");

    private final ModelPartRenderHandler<T> model = new ModelPartRenderHandler<>();

    public SkylineAerodyneEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.2f;
    }

    @Override
    protected ResourceLocation getModelId() {
        return ID;
    }

    @Override
    protected ModelPartRenderHandler<T> getModel(AircraftEntity entity) {
        return model;
    }
}
