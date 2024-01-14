package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.entity.misc.AircraftProperties;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class QuadrocopterEntity extends Rotorcraft {
    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(0.15f)
            .setPitchSpeed(1.5f)
            .setEngineSpeed(0.0325f)
            .setVerticalSpeed(0.0325f)
            .setGlideFactor(0.0f)
            .setDriftDrag(0.005f)
            .setLift(0.1f)
            .setRollFactor(15.0f)
            .setWindSensitivity(0.0125f)
            .setMass(1.0f);

    private static final VehicleInventoryDescription inventoryDescription = new VehicleInventoryDescription()
            .addSlot(VehicleInventoryDescription.SlotType.BOILER, 8 + 9, 8 + 14)
            .addSlot(VehicleInventoryDescription.SlotType.WEAPON, 8 + 18 * 2 + 6, 8 + 6)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 18 * 2 + 6 + 22, 8 + 6)
            .addSlots(VehicleInventoryDescription.SlotType.INVENTORY, 8 + 18 * 5, 8, 3, 2)
            .build();

    @Override
    public VehicleInventoryDescription getInventoryDescription() {
        return inventoryDescription;
    }

    public QuadrocopterEntity(EntityType<? extends AircraftEntity> entityType, Level world) {
        super(entityType, world, true);

        adaptPlayerRotation = false;
    }

    @Override
    protected float getBaseFuelConsumption() {
        return 0.5f;
    }

    @Override
    protected float getInputInterpolationSteps() {
        return 5;
    }

    protected SoundEvent getEngineSound() {
        return Sounds.PROPELLER_TINY.get();
    }

    @Override
    public AircraftProperties getProperties() {
        return properties;
    }

    @Override
    protected float getGroundVelocityDecay() {
        return 0.25f;
    }

    @Override
    protected float getHorizontalVelocityDelay() {
        return 0.9f;
    }

    @Override
    protected float getVerticalVelocityDelay() {
        return 0.8f;
    }

    @Override
    protected float getStabilizer() {
        return 0.1f;
    }

    @Override
    public Item asItem() {
        return Items.QUADROCOPTER.get();
    }

    final List<List<Vec3>> PASSENGER_POSITIONS = List.of(
            List.of(
                    new Vec3(0.0f, 0.275f, -0.1f)
            )
    );

    protected List<List<Vec3>> getPassengerPositions() {
        return PASSENGER_POSITIONS;
    }

    @Override
    protected float getGravity() {
        return wasTouchingWater ? 0.04f : (1.0f - getEnginePower()) * super.getGravity();
    }

    @Override
    protected void updateController() {
        setEngineTarget(1.0f);

        // forwards-backwards
        if (!onGround) {
            setXRot(getXRot() + getProperties().getPitchSpeed() * pressingInterpolatedZ.getSmooth());
        }
        setXRot(getXRot() * (1.0f - getStabilizer()));

        // up and down
        setDeltaMovement(getDeltaMovement().add(0.0f, getEnginePower() * properties.getVerticalSpeed() * pressingInterpolatedY.getSmooth(), 0.0f));

        // Rotate to pilot's head rotation
        Entity pilot = getControllingPassenger();
        if (pilot != null) {
            float diff = pilot.getYHeadRot() - getYRot();
            if (diff > 180.0f) {
                diff -= 360.0f;
            } else if (diff < -180.0f) {
                diff += 360.0f;
            }
            diff = diff * properties.getYawSpeed();
            setYRot(getYRot() + diff);
        }

        float thrust = (float) (Math.pow(getEnginePower(), 5.0) * properties.getEngineSpeed());

        // left and right
        Vec3 direction = getRightDirection().scale(thrust * pressingInterpolatedX.getSmooth());
        setDeltaMovement(getDeltaMovement().add(direction));

        // forward and backward
        direction = getForwardDirection().scale(thrust * pressingInterpolatedZ.getSmooth());
        setDeltaMovement(getDeltaMovement().add(direction));
    }

    protected void convertPower(Vec3 direction) {
        // Quadrocopters does not convert power
    }
}
