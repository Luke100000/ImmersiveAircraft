package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.AirshipEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CargoAirshipEntityRenderer<T extends AirshipEntity> extends AirshipEntityRenderer<T> {
    private static final ResourceLocation ID = Main.locate("cargo_airship");

    protected ResourceLocation getModelId() {
        return ID;
    }

    public CargoAirshipEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
    }
}
