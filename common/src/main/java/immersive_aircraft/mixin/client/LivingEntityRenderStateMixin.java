package immersive_aircraft.mixin.client;

import immersive_aircraft.client.render.entity.renderer.utils.PassengerLivingEntityRenderState;
import immersive_aircraft.entity.misc.PassengerPositionDescriptor;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements PassengerLivingEntityRenderState {
    @Unique
    private Float immersive_aircraft$vehicleXRot;

    @Unique
    private float immersive_aircraft$vehicleYRot;

    @Unique
    private Float immersive_aircraft$vehicleZRot;

    @Unique
    private @Nullable PassengerPositionDescriptor immersive_aircraft$passengerPosition;

    @Override
    public Float immersive_aircraft$getVehicleXRot() {
        return immersive_aircraft$vehicleXRot;
    }

    @Override
    public Float immersive_aircraft$getVehicleZRot() {
        return immersive_aircraft$vehicleZRot;
    }

    @Override
    public float immersive_aircraft$getVehicleYRot() {
        return immersive_aircraft$vehicleYRot;
    }

    @Override
    public @Nullable PassengerPositionDescriptor immersive_aircraft$getPassengerPosition() {
        return immersive_aircraft$passengerPosition;
    }

    @Override
    public void immersive_aircraft$setVehicleXRot(Float value) {
        immersive_aircraft$vehicleXRot = value;
    }

    @Override
    public void immersive_aircraft$setVehicleZRot(Float value) {
        immersive_aircraft$vehicleZRot = value;
    }

    @Override
    public void immersive_aircraft$setVehicleYRot(float value) {
        immersive_aircraft$vehicleYRot = value;
    }

    @Override
    public void immersive_aircraft$setPassengerPosition(@Nullable PassengerPositionDescriptor position) {
        immersive_aircraft$passengerPosition = position;
    }
}
