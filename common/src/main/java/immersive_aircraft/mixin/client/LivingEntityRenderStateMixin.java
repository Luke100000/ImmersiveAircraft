package immersive_aircraft.mixin.client;

import immersive_aircraft.client.render.entity.renderer.utils.PassengerLivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements PassengerLivingEntityRenderState {
    @Unique
    public Float immersive_aircraft$vehicleXRot = null;
    @Unique
    public Float immersive_aircraft$vehicleZRot = null;

    @Override
    public Float immersive_aircraft$getVehicleXRot() {
        return immersive_aircraft$vehicleXRot;
    }

    @Override
    public Float immersive_aircraft$getVehicleZRot() {
        return immersive_aircraft$vehicleZRot;
    }

    @Override
    public void immersive_aircraft$setVehicleXRot(Float f) {
        this.immersive_aircraft$vehicleXRot = f;
    }

    @Override
    public void immersive_aircraft$setVehicleZRot(Float f) {
        this.immersive_aircraft$vehicleZRot = f;
    }
}

