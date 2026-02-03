package immersive_aircraft.client.state;

public interface VehicleRenderState {
    void ia$setVehiclePitch(float pitch);

    void ia$setVehicleRoll(float roll);

    float ia$getVehiclePitch();

    float ia$getVehicleRoll();
}
