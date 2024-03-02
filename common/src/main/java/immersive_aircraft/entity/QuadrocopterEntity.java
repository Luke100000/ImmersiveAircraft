package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.item.upgrade.AircraftStat;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
    protected float getGravity() {
        return wasTouchingWater ? 0.04f : (1.0f - getEnginePower()) * super.getGravity();
    }

    @Override
    protected void updateController() {
        setEngineTarget(1.0f);

        // forwards-backwards
        if (!onGround) {
            setXRot(getXRot() + getProperties().get(AircraftStat.PITCH_SPEED) * pressingInterpolatedZ.getSmooth());
        }
        setXRot(getXRot() * (1.0f - getProperties().getAdditive(AircraftStat.STABILIZER)));

        // up and down
        setDeltaMovement(getDeltaMovement().add(0.0f, getEnginePower() * getProperties().get(AircraftStat.VERTICAL_SPEED) * pressingInterpolatedY.getSmooth(), 0.0f));

        // Rotate to pilot's head rotation
        Entity pilot = getControllingPassenger();
        if (pilot != null) {
            float diff = pilot.getYHeadRot() - getYRot();
            if (diff > 180.0f) {
                diff -= 360.0f;
            } else if (diff < -180.0f) {
                diff += 360.0f;
            }
            diff = diff * getProperties().get(AircraftStat.YAW_SPEED);
            setYRot(getYRot() + diff);
        }

        float thrust = (float) (Math.pow(getEnginePower(), 5.0) * getProperties().get(AircraftStat.ENGINE_SPEED));

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
