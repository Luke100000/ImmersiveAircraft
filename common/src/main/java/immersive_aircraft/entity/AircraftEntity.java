package immersive_aircraft.entity;

import com.mojang.math.Axis;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.misc.Trail;
import immersive_aircraft.entity.misc.TrailDescriptor;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.util.Utils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract aircraft, which performs basic physics
 */
public abstract class AircraftEntity extends EngineVehicle {
    protected double lastY;
    public float inWaterLevel;

    public AircraftEntity(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
    }

    private List<Trail> trails = Collections.emptyList();

    public List<Trail> getTrails() {
        if (getVehicleData().getTrails().size() != trails.size()) {
            trails = new ArrayList<>(getVehicleData().getTrails().size());
            for (TrailDescriptor trail : getVehicleData().getTrails()) {
                trails.add(new Trail(trail.length(), trail.gray()));
            }
        }
        return trails;
    }

    protected float getBaseTrailWidth(Matrix4f transform, int index, TrailDescriptor trail) {
        return 1.0f;
    }

    private void recordTrail(Matrix4f transform, int index, TrailDescriptor trail) {
        Matrix4f t = new Matrix4f(transform);
        t.translate(trail.x(), trail.y(), trail.z());
        if (trail.rotate() != 0.0) {
            t.rotate(Axis.ZP.rotationDegrees(engineRotation.getSmooth() * trail.rotate()));
        }

        Vector4f p0 = mulXVec(t, -trail.size());
        Vector4f p1 = mulXVec(t, trail.size());

        float trailStrength = Math.max(0.0f, Math.min(1.0f, getBaseTrailWidth(t, index, trail)));
        getTrails().get(index).add(p0, p1, trailStrength);
    }

    private Vector4f mulXVec(Matrix4fc mat, float x) {
        return new Vector4f(
                Math.fma(mat.m00(), x, mat.m30()),
                Math.fma(mat.m01(), x, mat.m31()),
                Math.fma(mat.m02(), x, mat.m32()),
                Math.fma(mat.m03(), x, mat.m33())
        );
    }

    @Override
    public void tick() {
        // rolling interpolation
        prevRoll = roll;
        if (onGround()) {
            setZRot(roll * 0.9f);
        } else {
            setZRot(-pressingInterpolatedX.getSmooth() * getProperties().get(VehicleStat.ROLL_FACTOR) * (1.0f - inWaterLevel));
        }

        // Fixes broken states
        if (Double.isNaN(getDeltaMovement().x) || Double.isNaN(getDeltaMovement().y) || Double.isNaN(getDeltaMovement().z)) {
            setDeltaMovement(0, 0, 0);
        }

        // Trails
        List<TrailDescriptor> trailDescriptors = getVehicleData().getTrails();
        if (!trailDescriptors.isEmpty()) {
            Matrix4f vehicleTransform = getVehicleTransform();
            for (int i = 0; i < trailDescriptors.size(); i++) {
                TrailDescriptor trail = trailDescriptors.get(i);
                recordTrail(vehicleTransform, i, trail);
            }
        }

        // Water
        if (wasTouchingWater) {
            inWaterLevel = Math.min(1.0f, inWaterLevel + 0.05f);
        } else {
            inWaterLevel = Math.max(0.0f, inWaterLevel - 0.05f);
        }

        super.tick();
    }

    protected void convertPower(Vec3 direction) {
        Vec3 velocity = getDeltaMovement();
        double drag = Math.abs(direction.dot(velocity.normalize()));
        setDeltaMovement(velocity.normalize()
                .lerp(direction, getProperties().get(VehicleStat.LIFT))
                .scale(velocity.length() * (drag * getProperties().get(VehicleStat.FRICTION) + (1.0 - getProperties().get(VehicleStat.FRICTION)))));
    }

    @Override
    protected float getGroundDecay() {
        float gravity = Math.min(1.0f, Math.max(0.0f, (float) getGravity() / (-0.04f)));
        float upgrade = Math.min(1.0f, getProperties().get(VehicleStat.ACCELERATION) * 0.5f);
        return (super.getGroundDecay() * gravity + (1.0f - gravity)) * (1.0f - upgrade) + upgrade;
    }

    @Override
    protected void updateController() {
        // left-right
        setYRot(getYRot() - getProperties().get(VehicleStat.YAW_SPEED) * pressingInterpolatedX.getSmooth());

        // forwards-backwards
        if (!onGround()) {
            setXRot(getXRot() + getProperties().get(VehicleStat.PITCH_SPEED) * pressingInterpolatedZ.getSmooth());
        }
        // Stabilizer reduces wind effects by 75% (not 100%)
        setXRot(getXRot() * (1.0f - getProperties().getAdditive(VehicleStat.STABILIZER) * 0.75f));
    }

    @Override
    protected void updateVelocity() {
        // get the direction
        Vector3f direction = getForwardDirection();

        // glide
        float diff = (float) (lastY - getY());
        if (lastY != 0.0 && getProperties().get(VehicleStat.GLIDE_FACTOR) > 0 && diff != 0.0) {
            setDeltaMovement(getDeltaMovement().add(toVec3d(direction).scale(diff * getProperties().get(VehicleStat.GLIDE_FACTOR) * (1.0f - Math.abs(direction.y)))));
        }
        lastY = (float) getY();

        // convert power
        convertPower(toVec3d(direction));

        // friction
        applyFriction();

        if (onGround()) {
            // Landing
            setXRot((getXRot() + getProperties().get(VehicleStat.GROUND_PITCH)) * 0.9f - getProperties().get(VehicleStat.GROUND_PITCH));
        } else if (!wasTouchingWater) {
            // Wind
            Vector3f effect = getWindEffect();
            setXRot(getXRot() + effect.x);
            setYRot(getYRot() + effect.z);

            float offsetStrength = 0.005f;
            setDeltaMovement(getDeltaMovement().add(effect.x * offsetStrength, 0.0f, effect.z * offsetStrength));
        }
    }

    public void chill() {
        lastY = 0.0f;
    }

    public float getWindStrength() {
        float sensitivity = getProperties().get(VehicleStat.WIND);
        float thundering = level().getRainLevel(0.0f);
        float raining = level().getThunderLevel(0.0f);
        float weather = (float) ((Config.getInstance().windClearWeather + getDeltaMovement().length()) + thundering * Config.getInstance().windThunderWeather + raining * Config.getInstance().windRainWeather);
        return weather * sensitivity;
    }

    public Vector3f getWindEffect() {
        float wind = getWindStrength();
        float nx = (float) (Utils.cosNoise(tickCount / 20.0 / getProperties().get(VehicleStat.MASS)) * wind);
        float nz = (float) (Utils.cosNoise(tickCount / 21.0 / getProperties().get(VehicleStat.MASS)) * wind);
        return new Vector3f(nx, 0.0f, nz);
    }
}

