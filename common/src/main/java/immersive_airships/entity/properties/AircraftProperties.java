package immersive_airships.entity.properties;

import immersive_airships.entity.AirshipEntity;

public class AircraftProperties {
    private final AirshipEntity airship;

    private float yawSpeed, pitchSpeed, pushSpeed, engineSpeed, glideFactor, maxPitch, driftDrag, lift, verticalSpeed, windSensitivity;
    private float gravity = -0.04f;

    public AircraftProperties(AirshipEntity airship) {
        this.airship = airship;
    }

    public float getYawSpeed() {
        return yawSpeed;
    }

    public AircraftProperties setYawSpeed(float yawSpeed) {
        this.yawSpeed = yawSpeed;
        return this;
    }

    public float getPitchSpeed() {
        return pitchSpeed;
    }

    public AircraftProperties setPitchSpeed(float pitchSpeed) {
        this.pitchSpeed = pitchSpeed;
        return this;
    }

    public float getPushSpeed() {
        return pushSpeed;
    }

    public AircraftProperties setPushSpeed(float pushSpeed) {
        this.pushSpeed = pushSpeed;
        return this;
    }

    public float getEngineSpeed() {
        return engineSpeed;
    }

    public AircraftProperties setEngineSpeed(float engineSpeed) {
        this.engineSpeed = engineSpeed;
        return this;
    }

    public float getGlideFactor() {
        return glideFactor;
    }

    public AircraftProperties setGlideFactor(float glideFactor) {
        this.glideFactor = glideFactor;
        return this;
    }

    public float getMaxPitch() {
        return maxPitch;
    }

    public AircraftProperties setMaxPitch(float maxPitch) {
        this.maxPitch = maxPitch;
        return this;
    }

    public float getDriftDrag() {
        return driftDrag;
    }

    public AircraftProperties setDriftDrag(float driftDrag) {
        this.driftDrag = driftDrag;
        return this;
    }

    public float getLift() {
        return lift;
    }

    public AircraftProperties setLift(float lift) {
        this.lift = lift;
        return this;
    }

    public float getVerticalSpeed() {
        return verticalSpeed;
    }

    public AircraftProperties setVerticalSpeed(float verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
        return this;
    }

    public float getWindSensitivity() {
        return windSensitivity;
    }

    public AircraftProperties setWindSensitivity(float windSensitivity) {
        this.windSensitivity = windSensitivity;
        return this;
    }

    public float getGravity() {
        return gravity;
    }

    public AircraftProperties setGravity(float gravity) {
        this.gravity = gravity;
        return this;
    }
}
