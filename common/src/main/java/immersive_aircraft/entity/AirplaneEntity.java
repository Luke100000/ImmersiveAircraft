package immersive_aircraft.entity;

import immersive_aircraft.entity.properties.AircraftProperties;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Implements airplane like physics properties and accelerated towards
 */
public abstract class AirplaneEntity extends EngineAircraft {
    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(5.0f)
            .setPitchSpeed(5.0f)
            .setEngineSpeed(0.0125f)
            .setGlideFactor(0.05f)
            .setDriftDrag(0.01f)
            .setLift(0.15f)
            .setRollFactor(45.0f)
            .setGroundPitch(4.0f)
            .setWheelFriction(0.05f)
            .setBrakeFactor(0.975f)
            .setWindSensitivity(0.01f)
            .setMass(15.0f);

    public AirplaneEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public AircraftProperties getProperties() {
        return properties;
    }

    @Override
    protected float getGravity() {
        Vec3d direction = getDirection();
        float speed = (float)((float)getVelocity().length() * (1.0f - Math.abs(direction.getY())));
        return Math.max(0.0f, 1.0f - speed * 1.75f) * super.getGravity();
    }

    private void updateEnginePowerTooltip() {
        if (getPrimaryPassenger() instanceof ClientPlayerEntity player) {
            player.sendMessage(new LiteralText(String.valueOf(getEnginePower())), true);
        }
    }

    @Override
    void updateController() {
        if (!hasPassengers()) {
            return;
        }

        super.updateController();

        // engine control
        if (movementY != 0) {
            setEngineTarget(Math.max(0.0f, Math.min(1.0f, getEngineTarget() + 0.1f * movementY)));
            updateEnginePowerTooltip();
            if (movementY < 0) {
                setVelocity(getVelocity().multiply(getProperties().getBrakeFactor()));
            }
        }

        // get direction
        Vec3d direction = getDirection();

        // speed
        float thrust = (float)(Math.pow(getEnginePower(), 2.0) * properties.getEngineSpeed());

        // accelerate
        setVelocity(getVelocity().add(direction.multiply(thrust)));
    }
}
