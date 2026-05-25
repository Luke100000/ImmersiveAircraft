package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Render state for vehicle entities. Stores a reference to the entity
 * so that the complex BB model rendering pipeline can access entity data
 * during submit(). The entity reference is only valid for the current frame.
 */
public class VehicleEntityRenderState extends EntityRenderState {
    public VehicleEntity entity;
    public float yaw;
    public float tickDelta;
}
