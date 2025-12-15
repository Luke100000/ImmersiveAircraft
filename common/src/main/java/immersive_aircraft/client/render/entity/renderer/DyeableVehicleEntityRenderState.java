package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

@Environment(EnvType.CLIENT)
public class DyeableVehicleEntityRenderState extends VehicleEntityRenderState {
    public int dyeColor;
    public int bodyColor;
    public int highlightColor;

    public DyeableVehicleEntityRenderState() {
        super();
    }
}
