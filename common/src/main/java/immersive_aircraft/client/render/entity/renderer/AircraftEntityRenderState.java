package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.entity.misc.Trail;
import immersive_aircraft.util.InterpolatedFloat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class AircraftEntityRenderState extends InventoryVehicleEntityRenderState {
    public Vector3f windEffect;
    public final List<Trail> trails;
    public float enginePower;
    public int tickCount;

    public AircraftEntityRenderState() {
        super();
        this.trails = new ArrayList<>();
    }
}
