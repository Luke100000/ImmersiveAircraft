package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.entity.misc.AircraftProperties;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class QuadrocopterEntity extends Rotorcraft {
    private final AircraftProperties properties = new AircraftProperties()
            .setYawSpeed(5.0f)
            .setPitchSpeed(1.5f)
            .setEngineSpeed(0.0325f)
            .setVerticalSpeed(0.0325f)
            .setGlideFactor(0.0f)
            .setDriftDrag(0.005f)
            .setLift(0.1f)
            .setRollFactor(15.0f)
            .setWindSensitivity(0.01f)
            .setMass(4.0f);

    public QuadrocopterEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    float getInputInterpolationSteps() {
        return 5;
    }

    SoundEvent getEngineSound() {
        return Sounds.PROPELLER_TINY.get();
    }

    @Override
    float getEnginePitch() {
        return 1.0f;
    }

    @Override
    public AircraftProperties getProperties() {
        return properties;
    }

    @Override
    float getGroundVelocityDecay() {
        return 0.25f;
    }

    @Override
    float getHorizontalVelocityDelay() {
        return 0.9f;
    }

    @Override
    float getVerticalVelocityDelay() {
        return 0.8f;
    }

    @Override
    float getStabilizer() {
        return 0.1f;
    }

    @Override
    public Item asItem() {
        return Items.QUADROCOPTER.get();
    }

    final List<List<Vec3d>> PASSENGER_POSITIONS = List.of(
            List.of(
                    new Vec3d(0.0f, 0.275f, -0.1f)
            )
    );

    protected List<List<Vec3d>> getPassengerPositions() {
        return PASSENGER_POSITIONS;
    }

    @Override
    protected float getGravity() {
        return touchingWater ? 0.04f : (1.0f - getEnginePower()) * super.getGravity();
    }

    @Override
    void updateController() {
        super.updateController();

        setEngineTarget(1.0f);

        // up and down
        setVelocity(getVelocity().add(0.0f, getEnginePower() * properties.getVerticalSpeed() * pressingInterpolatedY.getSmooth(), 0.0f));

        // get pointing direction
        Vec3d direction = getDirection();

        // accelerate
        float thrust = (float)(Math.pow(getEnginePower(), 5.0) * properties.getEngineSpeed()) * pressingInterpolatedZ.getSmooth();
        setVelocity(getVelocity().add(direction.multiply(thrust)));
    }
}
