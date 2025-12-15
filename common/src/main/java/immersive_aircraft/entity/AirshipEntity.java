package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.entity.misc.TrailDescriptor;
import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class AirshipEntity extends Rotorcraft {
    public AirshipEntity(EntityType<? extends AircraftEntity> entityType, Level world) {
        super(entityType, world, true);
    }

    @Override
    protected float getEngineReactionSpeed() {
        return 50.0f;
    }

    protected SoundEvent getEngineSound() {
        return Sounds.PROPELLER_SMALL.get();
    }

    @Override
    public Item asItem() {
        return Items.AIRSHIP.get();
    }

    @Override
    protected double getDefaultGravity() {
        return wasTouchingWater ? -0.04f : (1.0f - getEnginePower()) * super.getDefaultGravity();
    }

    @Override
    protected void updateController() {
        super.updateController();

        if (canTurnOnEngine(getControllingPassenger())) {
            setEngineTarget(1.0f);
        }

        // up and down
        setDeltaMovement(getDeltaMovement().add(0.0f, getEnginePower() * getProperties().get(VehicleStat.VERTICAL_SPEED) * pressingInterpolatedY.getSmooth(), 0.0f));

        // get pointing direction
        Vector3f direction = getForwardDirection();

        // accelerate
        float thrust = (float) (Math.pow(getEnginePower(), 5.0) * getProperties().get(VehicleStat.ENGINE_SPEED)) * pressingInterpolatedZ.getSmooth();
        Vector3f f = direction.mul(thrust);
        setDeltaMovement(getDeltaMovement().add(f.x, f.y, f.z));
    }

    @Override
    protected float getBaseTrailWidth(Matrix4f transform, int index, TrailDescriptor trail) {
        return Math.max(0.0f, Math.min(1.0f, (float) (getDeltaMovement().length() - 0.05f)));
    }

    @Override
    public void tick() {
        super.tick();

        float power = getEnginePower();

        if (level().isClientSide() && isWithinParticleRange() && power > 0.01) {
            Matrix4f transform = getVehicleTransform();

            // Smoke
            if (tickCount % 2 == 0) {
                Vector4f p = transformPosition(transform, (random.nextFloat() - 0.5f) * 0.4f, 0.8f, -0.8f);
                Vec3 velocity = getDeltaMovement();
                level().addParticle(ParticleTypes.SMOKE, p.x, p.y, p.z, velocity.x, velocity.y, velocity.z);
            }
        }
    }

    @Override
    public double getZoom() {
        return 5.0;
    }

    @Override
    public float getPropellerSpeed() {
        return super.getPropellerSpeed() * (0.25f + Math.abs(pressingInterpolatedZ.get(0.0f)));
    }
}
