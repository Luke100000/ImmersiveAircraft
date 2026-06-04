package immersive_aircraft.mixin.client;

import immersive_aircraft.client.render.entity.renderer.utils.PassengerLivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements PassengerLivingEntityRenderState {
    @Unique
    private Float immersive_aircraft$vehicleXRot;

    @Unique
    private Float immersive_aircraft$vehicleZRot;

    @Override
    public Float immersive_aircraft$getVehicleXRot() {
        return immersive_aircraft$vehicleXRot;
    }

    @Override
    public Float immersive_aircraft$getVehicleZRot() {
        return immersive_aircraft$vehicleZRot;
    }

    @Override
    public void immersive_aircraft$setVehicleXRot(Float value) {
        immersive_aircraft$vehicleXRot = value;
    }

    @Override
    public void immersive_aircraft$setVehicleZRot(Float value) {
        immersive_aircraft$vehicleZRot = value;
    }
}
