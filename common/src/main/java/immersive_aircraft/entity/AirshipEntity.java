package immersive_aircraft.entity;

import com.mojang.math.Axis;
import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.entity.misc.Trail;
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

import java.util.List;

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

    private final List<Trail> trails = List.of(new Trail(15, 0.5f));

    public List<Trail> getTrails() {
        return trails;
    }

    void trail(Matrix4f transform) {
        trail(transform, 0);
    }

    void trail(Matrix4f transform, int index) {
        trail(transform, index, 0.15f);
    }

    void trail(Matrix4f transform, int index, float thickness) {
        Vector4f p0 = transformPosition(transform, -thickness, 0.0f, 0.0f);
        Vector4f p1 = transformPosition(transform, thickness, 0.0f, 0.0f);

        float trailStrength = Math.max(0.0f, Math.min(1.0f, (float) (getDeltaMovement().length() - 0.05f)));
        getTrails().get(index).add(p0, p1, trailStrength);
    }

    @Override
    protected float getGravity() {
        return wasTouchingWater ? 0.04f : (1.0f - getEnginePower()) * super.getGravity();
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
    public void tick() {
        super.tick();

        float power = getEnginePower();

        if (level().isClientSide) {
            if (isWithinParticleRange() && power > 0.01) {
                Matrix4f transform = getVehicleTransform();

                // Trails
                addTrails(transform);

                // Smoke
                if (tickCount % 2 == 0) {
                    Vector4f p = transformPosition(transform, (random.nextFloat() - 0.5f) * 0.4f, 0.8f, -0.8f);
                    Vec3 velocity = getDeltaMovement();
                    level().addParticle(ParticleTypes.SMOKE, p.x, p.y, p.z, velocity.x, velocity.y, velocity.z);
                }
            } else {
                trails.get(0).add(ZERO_VEC4, ZERO_VEC4, 0.0f);
            }
        }
    }

    protected void addTrails(Matrix4f transform) {
        Matrix4f tr = new Matrix4f(transform);
        tr.translate(new Vector3f(0.0f, 0.4f, -1.2f));
        tr.rotate(Axis.ZP.rotationDegrees(engineRotation.getSmooth() * 50.0f));
        trail(tr);
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
