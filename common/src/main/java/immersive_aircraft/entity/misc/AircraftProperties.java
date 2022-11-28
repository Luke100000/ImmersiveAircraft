package immersive_aircraft.entity.misc;

import immersive_aircraft.entity.AircraftEntity;

public class AircraftProperties {
    private final AircraftEntity aircraft;

    private float yawSpeed, pitchSpeed, engineSpeed, glideFactor, driftDrag, lift, verticalSpeed, windSensitivity, wheelFriction, rollFactor, groundPitch, stabilizer, brakeFactor, mass;

    public AircraftProperties(AircraftEntity aircraft) {
        this.aircraft = aircraft;
    }

    public float getYawSpeed() {
        return yawSpeed;
    }

    // Speed of rotation
    public AircraftProperties setYawSpeed(float yawSpeed) {
        this.yawSpeed = yawSpeed;
        return this;
    }

    public float getPitchSpeed() {
        return pitchSpeed;
    }

    // Speed of up and down movement
    public AircraftProperties setPitchSpeed(float pitchSpeed) {
        this.pitchSpeed = pitchSpeed;
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

    public float getDriftDrag() {
        return driftDrag;
    }

    // How much energy is lost by drift drag
    public AircraftProperties setDriftDrag(float driftDrag) {
        this.driftDrag = driftDrag;
        return this;
    }

    public float getLift() {
        return lift;
    }

    // How strong the existing velocity can be transformed into the new direction
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

    public float getWheelFriction() {
        return wheelFriction;
    }

    public AircraftProperties setWheelFriction(float wheelFriction) {
        this.wheelFriction = wheelFriction;
        return this;
    }

    public float getRollFactor() {
        return rollFactor;
    }

    public AircraftProperties setRollFactor(float rollFactor) {
        this.rollFactor = rollFactor;
        return this;
    }

    public float getGroundPitch() {
        return groundPitch;
    }

    public AircraftProperties setGroundPitch(float groundPitch) {
        this.groundPitch = groundPitch;
        return this;
    }

    public float getStabilizer() {
        return stabilizer;
    }

    public AircraftProperties setStabilizer(float stabilizer) {
        this.stabilizer = stabilizer;
        return this;
    }

    public float getBrakeFactor() {
        return brakeFactor;
    }

    public AircraftProperties setBrakeFactor(float brakeFactor) {
        this.brakeFactor = brakeFactor;
        return this;
    }

    public float getMass() {
        return mass;
    }

    public AircraftProperties setMass(float mass) {
        this.mass = mass;
        return this;
    }
}
