package immersive_aircraft.entity;

import immersive_aircraft.Sounds;
import immersive_aircraft.entity.misc.AircraftProperties;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class GyrodyneEntity extends Rotorcraft {
    private final static float PUSH_SPEED = 0.05f;

    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(5.0f)
            .setPitchSpeed(3.0f)
            .setEngineSpeed(0.05f)
            .setVerticalSpeed(0.05f)
            .setDriftDrag(0.01f)
            .setLift(0.1f)
            .setRollFactor(30.0f)
            .setStabilizer(0.1f)
            .setWheelFriction(0.2f)
            .setWindSensitivity(0.005f)
            .setMass(5.0f);

    public GyrodyneEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    SoundEvent getEngineStartSound() {
        return Sounds.WOOSH.get();
    }

    SoundEvent getEngineSound() {
        return Sounds.WOOSH.get();
    }

    @Override
    public AircraftProperties getProperties() {
        return properties;
    }

    List<List<Vec3d>> PASSENGER_POSITIONS = List.of(
            List.of(
                    new Vec3d(0.0f, -0.8f, 0.3f)
            ),
            List.of(
                    new Vec3d(0.0f, -0.8f, 0.3f),
                    new Vec3d(0.0f, -0.8f, -0.6f)
            )
    );

    protected List<List<Vec3d>> getPassengerPositions() {
        return PASSENGER_POSITIONS;
    }

    @Override
    protected float getGravity() {
        return (1.0f - getEnginePower()) * super.getGravity();
    }

    @Override
    void updateController() {
        super.updateController();

        // launch that engine
        if (getEngineTarget() < 1.0f) {
            setEngineTarget(Math.max(0.0f, Math.min(1.0f, getEngineTarget() + pressingInterpolatedZ.getValue() * 0.02f)));
        }

        // up and down
        float power = getEnginePower() * properties.getVerticalSpeed() * pressingInterpolatedY.getSmooth();
        setVelocity(getVelocity().add(getTopDirection().multiply(power)));

        // get direction
        Vec3d direction = getDirection();

        // speed
        float sin = MathHelper.sin(getPitch() * ((float)Math.PI / 180));
        float thrust = (float)(Math.pow(getEnginePower(), 2.0) * properties.getEngineSpeed()) * sin;
        if (onGround) {
            thrust = PUSH_SPEED * pressingInterpolatedZ.getSmooth() * (pressingInterpolatedZ.getSmooth() > 0.0 ? 1.0f : 0.5f);
        }

        // accelerate
        setVelocity(getVelocity().add(direction.multiply(thrust)));
    }
}
