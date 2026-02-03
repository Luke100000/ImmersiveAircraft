package immersive_aircraft.mixin.client.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import immersive_aircraft.client.state.VehicleRenderState;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements VehicleRenderState {
    private float ia$vehiclePitch;
    private float ia$vehicleRoll;

    @Override
    public void ia$setVehiclePitch(float pitch) {
        this.ia$vehiclePitch = pitch;
    }

    @Override
    public void ia$setVehicleRoll(float roll) {
        this.ia$vehicleRoll = roll;
    }

    @Override
    public float ia$getVehiclePitch() {
        return ia$vehiclePitch;
    }

    @Override
    public float ia$getVehicleRoll() {
        return ia$vehicleRoll;
    }
}
