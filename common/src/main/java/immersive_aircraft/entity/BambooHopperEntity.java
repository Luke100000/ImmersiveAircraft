package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.entity.misc.TrailDescriptor;
import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class BambooHopperEntity extends AirplaneEntity {
    public BambooHopperEntity(EntityType<? extends AircraftEntity> entityType, Level world) {
        super(entityType, world, true);
    }

    @Override
    public Item asItem() {
        return Items.BAMBOO_HOPPER.get();
    }

    @Override
    public void tick() {
        super.tick();

        emitSmokeParticle(3.4375f, 1.125f, -0.25f, 0.0f, 0.0f, -0.2f);
        emitSmokeParticle(-3.4375f, 1.125f, -0.25f, 0.0f, 0.0f, -0.2f);


        float water = (float) fluidHeight.getDouble(FluidTags.WATER);
        if (water > 0) {
            emitSplashParticle(3.4375f, water, -0.5f, 0.0f, 0.0f, 0.0f);
            emitSplashParticle(-3.4375f, water, -0.5f, 0.0f, 0.0f, 0.0f);
        }
    }

    public void emitSplashParticle(float x, float y, float z, float nx, float ny, float nz) {
        if (!isWithinParticleRange() || !level().isClientSide) {
            return;
        }

        Matrix4f transform = getVehicleTransform();
        double length = Math.min(100, getSpeedVector().length() * 20.0f);
        while (length > 1.0) {
            length--;
            if (length > random.nextFloat()) {
                Vector4f p = transformPosition(transform, x + (random.nextFloat() - 0.5f), y, z - (random.nextFloat() - 0.0f));
                level().addParticle(ParticleTypes.BUBBLE, p.x, p.y, p.z, nx, ny, nz);
                level().addParticle(ParticleTypes.SPLASH, p.x, p.y, p.z, nx, ny, nz);
            }
        }
    }

    @Override
    protected float getGravity() {
        float water = (float) fluidHeight.getDouble(FluidTags.WATER);
        return water > 0 ? 0.04f * water : (1.0f - getEnginePower()) * super.getGravity();
    }

    @Override
    protected float getBaseTrailWidth(Matrix4f transform, int index, TrailDescriptor trail) {
        return (float) (enginePower.getSmooth() * getSpeedVector().length());
    }

    @Override
    protected void updateVelocity() {
        super.updateVelocity();

        // Landing on water
        if (wasTouchingWater) {
            setXRot((getXRot() + getProperties().get(VehicleStat.GROUND_PITCH)) * 0.9f - getProperties().get(VehicleStat.GROUND_PITCH));
        }
    }

    @Override
    public boolean worksUnderWater() {
        return true;
    }

    @Override
    protected float getDismountRotation() {
        return 0.0f;
    }

    @Override
    public double getZoom() {
        return 6.0;
    }

    @Override
    protected SoundEvent getEngineStartSound() {
        return Sounds.ENGINE_START_BAMBOO_HOPPER.get();
    }

    @Override
    protected SoundEvent getEngineSound() {
        return Sounds.PROPELLER_BAMBOO_HOPPER.get();
    }
}
