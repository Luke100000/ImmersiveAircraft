package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class QuadrocopterEntity extends Rotorcraft {
    public QuadrocopterEntity(EntityType<? extends AircraftEntity> entityType, Level world) {
        super(entityType, world, true);

        adaptPlayerRotation = false;
    }

    @Override
    protected float getInputInterpolationSteps() {
        return 5;
    }

    protected SoundEvent getEngineSound() {
        return Sounds.PROPELLER_TINY.get();
    }

    @Override
    public Item asItem() {
        return Items.QUADROCOPTER.get();
    }

    @Override
    protected double getDefaultGravity() {
        return wasTouchingWater ? -0.04f : (1.0f - getEnginePower()) * super.getDefaultGravity();
    }

    @Override
    protected void updateController() {
        if (canTurnOnEngine(getControllingPassenger())) {
            setEngineTarget(1.0f);
        }

        // forwards-backwards
        if (!onGround()) {
            setXRot(getXRot() + getProperties().get(VehicleStat.PITCH_SPEED) * pressingInterpolatedZ.getSmooth());
        }
        setXRot(getXRot() * (1.0f - getProperties().getAdditive(VehicleStat.STABILIZER)));

        // up and down
        setDeltaMovement(getDeltaMovement().add(0.0f, getEnginePower() * getProperties().get(VehicleStat.VERTICAL_SPEED) * pressingInterpolatedY.getSmooth(), 0.0f));

        // Rotate to pilot's head rotation
        Entity pilot = getControllingPassenger();
        if (pilot != null) {
            float diff = pilot.getYHeadRot() - getYRot();
            if (diff > 180.0f) {
                diff -= 360.0f;
            } else if (diff < -180.0f) {
                diff += 360.0f;
            }
            diff = diff * getProperties().get(VehicleStat.YAW_SPEED);
            if (Math.abs(diff) < 60f) {
                setYRot(getYRot() + diff);
            }
        }

        float thrust = (float) (Math.pow(getEnginePower(), 5.0) * getProperties().get(VehicleStat.ENGINE_SPEED));

        // left and right
        Vector3f direction = getRightDirection().mul(thrust * pressingInterpolatedX.getSmooth());
        setDeltaMovement(getDeltaMovement().add(direction.x, direction.y, direction.z));

        // forward and backward
        direction = getForwardDirection().mul(thrust * pressingInterpolatedZ.getSmooth());
        setDeltaMovement(getDeltaMovement().add(direction.x, direction.y, direction.z));
    }

    @Override
    protected void convertPower(Vec3 direction) {
        // Quadrocopters does not convert power
    }
}
