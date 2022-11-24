package immersive_aircraft.entity;

import immersive_aircraft.entity.properties.AircraftProperties;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class AirplaneEntity extends EngineAircraft {
    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(0.5f)
            .setPitchSpeed(0.75f)
            .setEngineSpeed(0.0125f)
            .setGlideFactor(0.05f)
            .setMaxPitch(60)
            .setDriftDrag(0.01f)
            .setLift(0.15f)
            .setRollFactor(10.0f)
            .setGroundPitch(5.0f)
            .setWheelFriction(0.1f)
            .setBrakeFactor(0.975f);

    public AirplaneEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    AircraftProperties getProperties() {
        return properties;
    }

    @Override
    protected float getGravity() {
        return (1.0f - getEnginePower()) * super.getGravity();
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
