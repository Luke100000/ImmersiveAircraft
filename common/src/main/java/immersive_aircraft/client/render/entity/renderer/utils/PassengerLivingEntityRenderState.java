package immersive_aircraft.client.render.entity.renderer.utils;

import immersive_aircraft.entity.misc.PassengerPositionDescriptor;
import org.jetbrains.annotations.Nullable;

public interface PassengerLivingEntityRenderState {
    Float immersive_aircraft$getVehicleXRot();

    float immersive_aircraft$getVehicleYRot();

    Float immersive_aircraft$getVehicleZRot();

    @Nullable PassengerPositionDescriptor immersive_aircraft$getPassengerPosition();

    void immersive_aircraft$setVehicleXRot(Float value);

    void immersive_aircraft$setVehicleYRot(float value);

    void immersive_aircraft$setVehicleZRot(Float value);

    void immersive_aircraft$setPassengerPosition(@Nullable PassengerPositionDescriptor position);
}
