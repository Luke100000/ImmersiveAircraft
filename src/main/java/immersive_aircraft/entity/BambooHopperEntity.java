package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.compat.Matrix4f;
import immersive_aircraft.compat.Vec3f;
import immersive_aircraft.compat.Vector4f;
import immersive_aircraft.entity.misc.AircraftProperties;
import immersive_aircraft.entity.misc.Trail;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.entity.misc.WeaponMount;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Collections;
import java.util.List;

public class BambooHopperEntity extends AirplaneEntity {
    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(3.5f)
            .setPitchSpeed(3.5f)
            .setEngineSpeed(0.03f)
            .setGlideFactor(0.075f)
            .setDriftDrag(0.01f)
            .setLift(0.15f)
            .setRollFactor(30.0f)
            .setGroundPitch(1.0f)
            .setWindSensitivity(0.04f)
            .setMass(2.0f);

    private static final VehicleInventoryDescription inventoryDescription = new VehicleInventoryDescription()
            .addSlot(VehicleInventoryDescription.SlotType.BOILER, 80, 42)
            .addSlot(VehicleInventoryDescription.SlotType.BOOSTER, 80, 70)
            .addSlot(VehicleInventoryDescription.SlotType.WEAPON, 48, 21)
            .addSlot(VehicleInventoryDescription.SlotType.WEAPON, 112, 21)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 48, 44)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 112, 44)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 48, 67)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 112, 67)
            .addSlots(VehicleInventoryDescription.SlotType.INVENTORY, 8, 18, 2, 4)
            .addSlots(VehicleInventoryDescription.SlotType.INVENTORY, 134, 18, 2, 4)
            .build();

    @Override
    public VehicleInventoryDescription getInventoryDescription() {
        return inventoryDescription;
    }

    private static final List<Map<WeaponMount.Type, List<WeaponMount>>> WEAPON_MOUNTS = buildWeaponMounts();

    private static List<Map<WeaponMount.Type, List<WeaponMount>>> buildWeaponMounts() {
        List<Map<WeaponMount.Type, List<WeaponMount>>> list = new ArrayList<>();
        Map<WeaponMount.Type, List<WeaponMount>> slot0 = new EnumMap<>(WeaponMount.Type.class);
        slot0.put(WeaponMount.Type.ROTATING, Arrays.asList(
                WeaponMount.of(3.4375f, 2.0625f, 0.75f, 0, 0, 0),
                WeaponMount.of(-3.4375f, 2.0625f, 0.75f, 0, 0, 0)));
        slot0.put(WeaponMount.Type.FRONT, Arrays.asList(
                WeaponMount.of(-2.0f, 1.5f, 1.5f, 0, 0, 180),
                WeaponMount.of(2.0f, 1.5f, 1.5f, 0, 0, 180)));
        slot0.put(WeaponMount.Type.DROP, Arrays.asList(
                WeaponMount.of(-2.0f, 1.5f, 1.5f, 0, 0, 0),
                WeaponMount.of(2.0f, 1.5f, 1.5f, 0, 0, 0)));
        list.add(slot0);
        Map<WeaponMount.Type, List<WeaponMount>> slot1 = new EnumMap<>(WeaponMount.Type.class);
        slot1.put(WeaponMount.Type.ROTATING, Arrays.asList(
                WeaponMount.of(3.4375f, 1.9375f, -1.5f, 0, 0, 0),
                WeaponMount.of(-3.4375f, 1.9375f, -1.5f, 0, 0, 0)));
        slot1.put(WeaponMount.Type.FRONT, Arrays.asList(
                WeaponMount.of(-5.0f, 1.5f, 1.5f, 0, 0, 180),
                WeaponMount.of(5.0f, 1.5f, 1.5f, 0, 0, 180)));
        slot1.put(WeaponMount.Type.DROP, Arrays.asList(
                WeaponMount.of(-5.0f, 1.5f, 1.5f, 0, 0, 0),
                WeaponMount.of(5.0f, 1.5f, 1.5f, 0, 0, 0)));
        list.add(slot1);
        return list;
    }

    @Override
    protected List<Map<WeaponMount.Type, List<WeaponMount>>> getWeaponMountDefinitions() {
        return WEAPON_MOUNTS;
    }

    private boolean wasInWater = false;

    public BambooHopperEntity(World world) {
        super(world);
        // 1.20.1 EntityType dimensions: 3.0 x 1.5, fire immune
        setSize(3.0f, 1.5f);
        isImmuneToFire = true;
    }

    @Override
    public Item asItem() {
        return Items.BAMBOO_HOPPER.get();
    }

    @Override
    public double getZoom() {
        return 6.0;
    }

    @Override
    public AircraftProperties getProperties() {
        return properties;
    }

    @Override
    protected float getBaseFuelConsumption() {
        return 1.5f;
    }

    @Override
    protected float getGroundVelocityDecay() {
        return falloffGroundVelocityDecay(0.925f);
    }

    @Override
    protected SoundEvent getEngineStartSound() {
        return Sounds.ENGINE_START_BAMBOO_HOPPER.get();
    }

    @Override
    protected SoundEvent getEngineSound() {
        return Sounds.PROPELLER_BAMBOO_HOPPER.get();
    }

    final List<List<Vec3d>> PASSENGER_POSITIONS = Arrays.asList(
            Collections.singletonList(
                    new Vec3d(0.0, 0.625, 0.9375)
            ),
            Arrays.asList(
                    new Vec3d(0.0, 0.625, 0.9375),
                    new Vec3d(0.4375, 0.625, -0.25)
            ),
            Arrays.asList(
                    new Vec3d(0.0, 0.625, 0.9375),
                    new Vec3d(0.4375, 0.625, -0.25),
                    new Vec3d(-0.4375, 0.625, -0.25)
            )
    );

    @Override
    protected List<List<Vec3d>> getPassengerPositions() {
        return PASSENGER_POSITIONS;
    }

    private final List<Trail> trails = Collections.singletonList(new Trail(20, 1.0f));

    @Override
    public List<Trail> getTrails() {
        return trails;
    }

    private void trail(Matrix4f transform) {
        Matrix4f tr = transform.copy();
        tr.multiplyByTranslation(0.0f, 0.75f, -2.25f);
        tr.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(engineRotation.getSmooth() * 48.0f));

        Vector4f p0 = transformPosition(tr, -0.25f, 0.0f, 0.0f);
        Vector4f p1 = transformPosition(tr, 0.25f, 0.0f, 0.0f);

        float trailStrength = Math.max(0.0f, Math.min(1.0f, (float) (getVelocity().length() - 0.05f)));
        trails.get(0).add(p0, p1, trailStrength);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (world.isRemote) {
            if (isWithinParticleRange()) {
                Matrix4f transform = getVehicleTransform();

                // Trail
                trail(transform);

                // Smoke
                emitSmokeParticle(transform, 3.4375f, 1.125f, -0.25f);
                emitSmokeParticle(transform, -3.4375f, 1.125f, -0.25f);

                // Splash when moving through water
                // TODO(deviation): 1.20.1 uses the actual fluid height for the splash y
                //  position; 1.12.2 only has the binary inWater flag, so a fixed height is used
                if (inWater) {
                    emitSplashParticle(transform, 3.4375f, 0.5f, -0.5f);
                    emitSplashParticle(transform, -3.4375f, 0.5f, -0.5f);
                }
            } else {
                trails.get(0).add(ZERO_VEC4, ZERO_VEC4, 0.0f);
            }
        }

        wasInWater = inWater;
    }

    private void emitSmokeParticle(Matrix4f transform, float x, float y, float z) {
        float power = getEnginePower();
        if (power > 0.05) {
            Vector4f p = transformPosition(transform, x, y, z);
            Vec3d velocity = getVelocity();
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, p.getX(), p.getY(), p.getZ(), velocity.x, velocity.y, velocity.z);
        }
    }

    private void emitSplashParticle(Matrix4f transform, float x, float y, float z) {
        double length = Math.min(100, getVelocity().length() * 20.0f);
        while (length > 1.0) {
            length--;
            if (length > rand.nextFloat()) {
                Vector4f p = transformPosition(transform, x + (rand.nextFloat() - 0.5f), y, z - (rand.nextFloat() - 0.0f));
                world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, p.getX(), p.getY(), p.getZ(), 0.0, 0.0, 0.0);
                world.spawnParticle(EnumParticleTypes.WATER_SPLASH, p.getX(), p.getY(), p.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected float getGravity() {
        return inWater ? 0.04f : (1.0f - getEnginePower()) * super.getGravity();
    }

    @Override
    protected void updateVelocity() {
        super.updateVelocity();

        // Landing on water
        if (wasInWater) {
            setPitch((getPitch() + getProperties().getGroundPitch()) * 0.9f - getProperties().getGroundPitch());
        }
    }
}
