package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.entity.misc.AircraftProperties;
import immersive_aircraft.entity.misc.Trail;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import net.minecraft.world.World;

import java.util.List;

public class AirshipEntity extends Rotorcraft {
    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(5.0f)
            .setEngineSpeed(0.02f)
            .setVerticalSpeed(0.025f)
            .setGlideFactor(0.0f)
            .setDriftDrag(0.01f)
            .setLift(0.1f)
            .setRollFactor(5.0f)
            .setWindSensitivity(0.05f)
            .setMass(3.0f);

    private static final VehicleInventoryDescription inventoryDescription = new VehicleInventoryDescription()
            .addSlot(VehicleInventoryDescription.SlotType.BOILER, 8 + 9, 8 + 36)
            .addSlot(VehicleInventoryDescription.SlotType.WEAPON, 8 + 18 * 2 + 6, 8 + 6)
            .addSlot(VehicleInventoryDescription.SlotType.WEAPON, 8 + 18 * 2 + 28, 8 + 6)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 18 * 2 + 6, 8 + 6 + 22)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 18 * 2 + 28, 8 + 6 + 22)
            .addSlot(VehicleInventoryDescription.SlotType.BANNER, 8 + 18 * 2 + 6, 8 + 6 + 22 * 2)
            .addSlot(VehicleInventoryDescription.SlotType.DYE, 8 + 18 * 2 + 28, 8 + 6 + 22 * 2)
            .addSlots(VehicleInventoryDescription.SlotType.INVENTORY, 8 + 18 * 5, 8, 4, 4)
            .build();

    @Override
    public VehicleInventoryDescription getInventoryDescription() {
        return inventoryDescription;
    }

    public AirshipEntity(EntityType<? extends AircraftEntity> entityType, World world) {
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
    public AircraftProperties getProperties() {
        return properties;
    }

    @Override
    protected float getGroundVelocityDecay() {
        return 0.5f;
    }

    @Override
    protected float getHorizontalVelocityDelay() {
        return 0.97f;
    }

    @Override
    protected float getVerticalVelocityDelay() {
        return 0.925f;
    }

    @Override
    protected float getStabilizer() {
        return 0.1f;
    }

    @Override
    public Item asItem() {
        return Items.AIRSHIP.get();
    }

    final List<List<Vec3d>> PASSENGER_POSITIONS = List.of(
            List.of(
                    new Vec3d(0.0f, -0.1f, 0.0f)
            ),
            List.of(
                    new Vec3d(0.0f, -0.1f, 0.4f),
                    new Vec3d(0.0f, -0.1f, -0.3f)
            )
    );

    private final List<Trail> trails = List.of(new Trail(15, 0.5f));

    public List<Trail> getTrails() {
        return trails;
    }

    void trail(Matrix4f transform) {
        trail(transform, 0);
    }

    void trail(Matrix4f transform, int index) {
        Vector4f p0 = transformPosition(transform, (float) 0.0 - 0.15f, 0.0f, 0.0f);
        Vector4f p1 = transformPosition(transform, (float) 0.0 + 0.15f, 0.0f, 0.0f);

        float trailStrength = Math.max(0.0f, Math.min(1.0f, (float) (getVelocity().length() - 0.05f)));
        getTrails().get(index).add(p0, p1, trailStrength);
    }

    protected List<List<Vec3d>> getPassengerPositions() {
        return PASSENGER_POSITIONS;
    }

    @Override
    protected float getGravity() {
        return touchingWater ? 0.04f : (1.0f - getEnginePower()) * super.getGravity();
    }

    @Override
    protected void updateController() {
        super.updateController();

        setEngineTarget(1.0f);

        // up and down
        setVelocity(getVelocity().add(0.0f, getEnginePower() * properties.getVerticalSpeed() * pressingInterpolatedY.getSmooth(), 0.0f));

        // get pointing direction
        Vec3d direction = getDirection();

        // accelerate
        float thrust = (float) (Math.pow(getEnginePower(), 5.0) * properties.getEngineSpeed()) * pressingInterpolatedZ.getSmooth();
        setVelocity(getVelocity().add(direction.multiply(thrust)));
    }

    @Override
    public void tick() {
        super.tick();

        float power = getEnginePower();

        if (world.isClient) {
            if (isWithinParticleRange() && power > 0.01) {
                Matrix4f transform = getVehicleTransform();

                // Trails
                addTrails(transform);

                // Smoke
                if (age % 2 == 0) {
                    Vector4f p = transformPosition(transform, (random.nextFloat() - 0.5f) * 0.4f, 0.8f, -0.8f);
                    Vec3d velocity = getVelocity();
                    world.addParticle(ParticleTypes.SMOKE, p.getX(), p.getY(), p.getZ(), velocity.x, velocity.y, velocity.z);
                }
            } else {
                trails.get(0).add(ZERO_VEC4, ZERO_VEC4, 0.0f);
            }
        }
    }

    protected void addTrails(Matrix4f transform) {
        Matrix4f tr = transform.copy();
        tr.multiplyByTranslation(0.0f, 0.4f, -1.2f);
        tr.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(engineRotation.getSmooth() * 50.0f));
        trail(tr);
    }
}
