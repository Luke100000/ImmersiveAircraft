package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.entity.misc.TrailDescriptor;
import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class BambooHopperEntity extends AirplaneEntity {
    private int fluidHeightTick = Integer.MIN_VALUE;
    private float cachedFluidHeight = 0.0f;

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
        float fluid = getFluidHeight();
        if (fluid > 0.0f) {
            emitSplashParticle(3.4375f, fluid, -0.5f, 0.0f, 0.0f, 0.0f);
            emitSplashParticle(-3.4375f, fluid, -0.5f, 0.0f, 0.0f, 0.0f);
        }
    }

    private float getFluidHeight() {
        if (fluidHeightTick == tickCount) {
            return cachedFluidHeight;
        }

        fluidHeightTick = tickCount;
        cachedFluidHeight = calculateFluidHeight();
        return cachedFluidHeight;
    }

    private float calculateFluidHeight() {
        AABB box = getBoundingBox().deflate(0.001D);

        int minX = Mth.floor(box.minX);
        int maxX = Mth.ceil(box.maxX);
        int minY = Mth.floor(box.minY);
        int maxY = Mth.ceil(box.maxY);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.ceil(box.maxZ);

        if (!level().hasChunksAt(minX, minY, minZ, maxX - 1, maxY - 1, maxZ - 1)) {
            return 0.0f;
        }

        double height = 0.0D;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    pos.set(x, y, z);

                    FluidState state = level().getFluidState(pos);
                    if (state.isEmpty()) {
                        continue;
                    }

                    double surface = y + state.getHeight(level(), pos);
                    if (surface >= box.minY) {
                        height = Math.max(height, surface - box.minY);
                    }
                }
            }
        }

        return (float) height;
    }

    public void emitSplashParticle(float x, float y, float z, float nx, float ny, float nz) {
        if (!isWithinParticleRange() || !level().isClientSide()) {
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
    protected double getDefaultGravity() {
        float fluid = getFluidHeight();
        return fluid > 0.0f ? -0.04 * fluid : (1.0 - getEnginePower()) * super.getDefaultGravity();
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
