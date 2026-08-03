package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.compat.Matrix3f;
import immersive_aircraft.compat.Matrix4f;
import immersive_aircraft.compat.Vec3f;
import immersive_aircraft.compat.Vector4f;
import immersive_aircraft.entity.misc.AircraftProperties;
import immersive_aircraft.entity.misc.Trail;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.entity.weapon.HeavyCrossbow;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.util.InterpolatedFloat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class WarshipEntity extends DyeableVehicleEntity {
    private final HeavyCrossbow turret;

    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(2.5f)
            .setEngineSpeed(0.035f)
            .setVerticalSpeed(0.025f)
            .setGlideFactor(0.0f)
            .setDriftDrag(0.01f)
            .setLift(0.1f)
            .setRollFactor(3.0f)
            .setWindSensitivity(0.01f)
            .setMass(10.0f);

    private static final VehicleInventoryDescription inventoryDescription = new VehicleInventoryDescription()
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 32, 70)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 54, 70)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 76, 70)
            .addSlot(VehicleInventoryDescription.SlotType.WEAPON, 102, 70)
            .addSlot(VehicleInventoryDescription.SlotType.DYE, 128, 70)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 32, 92)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 54, 92)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 76, 92)
            .addSlot(VehicleInventoryDescription.SlotType.WEAPON, 102, 92)
            .addSlot(VehicleInventoryDescription.SlotType.BOOSTER, 128, 92)
            .addSlot(VehicleInventoryDescription.SlotType.BOILER, 40, 40)
            .addSlot(VehicleInventoryDescription.SlotType.BOILER, 80, 40)
            .addSlot(VehicleInventoryDescription.SlotType.BOILER, 120, 40)
            .addBoxedSlots(VehicleInventoryDescription.SlotType.INVENTORY, -62, 8, 3, 11)
            .addBoxedSlots(VehicleInventoryDescription.SlotType.INVENTORY, 186, 8, 3, 11)
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
                WeaponMount.of(-3.875f, 4.625f, 1.825f, 90, -90, 0),
                WeaponMount.of(3.875f, 4.625f, 1.825f, -90, -90, 0),
                WeaponMount.of(-3.875f, 4.625f, -1.825f, 90, -90, 0),
                WeaponMount.of(3.875f, 4.625f, -1.825f, -90, -90, 0)));
        slot0.put(WeaponMount.Type.FRONT, Arrays.asList(
                WeaponMount.of(-3.875f, 4.625f, 1.825f, 0, 0, 90),
                WeaponMount.of(3.875f, 4.625f, 1.825f, 0, 0, -90),
                WeaponMount.of(-3.875f, 4.625f, -1.825f, 0, 0, 90),
                WeaponMount.of(3.875f, 4.625f, -1.825f, 0, 0, -90)));
        slot0.put(WeaponMount.Type.DROP, Arrays.asList(
                WeaponMount.of(-3.875f, 4.625f, 1.825f, 90, 90, 0),
                WeaponMount.of(3.875f, 4.625f, 1.825f, -90, 90, 0),
                WeaponMount.of(-3.875f, 4.625f, -1.825f, 90, 90, 0),
                WeaponMount.of(3.875f, 4.625f, -1.825f, -90, 90, 0)));
        list.add(slot0);
        Map<WeaponMount.Type, List<WeaponMount>> slot1 = new EnumMap<>(WeaponMount.Type.class);
        slot1.put(WeaponMount.Type.ROTATING, Arrays.asList(
                WeaponMount.of(-2.375f, 4.625f, 4.625f, 0, 90, 0),
                WeaponMount.of(2.375f, 4.625f, 4.625f, 0, 90, 0)));
        slot1.put(WeaponMount.Type.FRONT, Arrays.asList(
                WeaponMount.of(-2.375f, 4.875f, 4.375f, 0, 0, 0),
                WeaponMount.of(2.375f, 4.875f, 4.375f, 0, 0, 0)));
        slot1.put(WeaponMount.Type.DROP, Arrays.asList(
                WeaponMount.of(-2.375f, 4.25f, 4.25f, 0, 0, 0),
                WeaponMount.of(2.375f, 4.25f, 4.25f, 0, 0, 0)));
        list.add(slot1);
        return list;
    }

    @Override
    protected List<Map<WeaponMount.Type, List<WeaponMount>>> getWeaponMountDefinitions() {
        return WEAPON_MOUNTS;
    }

    public WarshipEntity(World world) {
        super(world);
        // 1.20.1 EntityType dimensions: 5.0 x 6.5, fire immune
        setSize(5.0f, 6.5f);
        isImmuneToFire = true;

        // The built-in bow turret (velocity 5.0, no inaccuracy; slot -1)
        turret = new HeavyCrossbow(this, new ItemStack(Items.HEAVY_CROSSBOW.get()), new WeaponMount(Matrix4f.scale(1.0f, 1.0f, 1.0f), false), -1, 5.0f, 0.0f);
    }

    public HeavyCrossbow getTurret() {
        return turret;
    }

    public final InterpolatedFloat turretYaw = new InterpolatedFloat(5);
    public final InterpolatedFloat turretPitch = new InterpolatedFloat(5);

    @Override
    public Item asItem() {
        return Items.WARSHIP.get();
    }

    @Override
    public double getZoom() {
        return 5.0f + enginePower.getSmooth() * 5.0f;
    }

    @Override
    public AircraftProperties getProperties() {
        return properties;
    }

    @Override
    protected float getEngineReactionSpeed() {
        return 100.0f;
    }

    @Override
    protected float getBaseFuelConsumption() {
        return 0.8f;
    }

    @Override
    protected float getDurability() {
        return 3.0f * getTotalUpgrade(AircraftStat.DURABILITY);
    }

    @Override
    protected SoundEvent getEngineSound() {
        return Sounds.WARSHIP.get();
    }

    @Override
    protected SoundEvent getEngineStartSound() {
        return Sounds.ENGINE_START_WARSHIP.get();
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

    final List<List<Vec3d>> PASSENGER_POSITIONS = Arrays.asList(
            Arrays.asList(
                    new Vec3d(0.0, 0.626, 0.6)
            ),
            Arrays.asList(
                    new Vec3d(0.0, 0.626, 0.6),
                    new Vec3d(0.0, 0.0, 0.0)
            ),
            Arrays.asList(
                    new Vec3d(0.0, 0.626, 0.6),
                    new Vec3d(0.0, 0.0, 0.0),
                    new Vec3d(0.0, 0.626, -0.3)
            ),
            Arrays.asList(
                    new Vec3d(0.0, 0.626, 0.6),
                    new Vec3d(0.0, 0.0, 0.0),
                    new Vec3d(0.3, 0.626, -0.25),
                    new Vec3d(-0.3, 0.626, -0.35)
            )
    );

    @Override
    protected List<List<Vec3d>> getPassengerPositions() {
        return PASSENGER_POSITIONS;
    }

    private final List<Trail> trails = Arrays.asList(
            new Trail(15, 0.25f),
            new Trail(14, 0.25f),
            new Trail(16, 0.25f),
            new Trail(14, 0.25f)
    );

    @Override
    public List<Trail> getTrails() {
        return trails;
    }

    private void warshipTrail(Matrix4f transform, int index) {
        Vector4f p0 = transformPosition(transform, -0.125f, 0.0f, 0.0f);
        Vector4f p1 = transformPosition(transform, 0.125f, 0.0f, 0.0f);

        float trailStrength = Math.max(0.0f, Math.min(1.0f, (float) (getVelocity().length() - 0.05f)));
        getTrails().get(index).add(p0, p1, trailStrength);
    }

    @Override
    protected void addTrails(Matrix4f transform) {
        trailAt(transform, 0, 0.0f, 1.4f, -2.5f);
        trailAt(transform, 1, 2.5f, 4.6f, -5.5f);
        trailAt(transform, 2, 0.0f, 4.6f, -5.5f);
        trailAt(transform, 3, -2.5f, 4.6f, -5.5f);
    }

    private void trailAt(Matrix4f transform, int index, float x, float y, float z) {
        Matrix4f tr = transform.copy();
        tr.multiplyByTranslation(x, y, z);
        tr.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(engineRotation.getSmooth() * 40.0f));
        warshipTrail(tr, index);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        // Built-in turret weapon (the gunner is the second passenger)
        turret.tick();

        Entity gunner = getTurretGunner();
        if (gunner != null) {
            turretYaw.update(MathHelper.clamp(MathHelper.wrapDegrees(gunner.getRotationYawHead() - getYaw()), -75f, 75f));
            turretPitch.update(MathHelper.clamp(gunner.rotationPitch, -30f, 30f));
        } else {
            turretYaw.update(0.0f);
            turretPitch.update(0.0f);
        }

        // Rotate turret mount
        float[] gunnerPosition = getGunnerPosition();
        float deg = (float) (180.0 / Math.PI);
        Matrix4f mountTransform = turret.getMount().transform();
        mountTransform.loadIdentity();
        mountTransform.multiplyByTranslation(gunnerPosition[0], gunnerPosition[1], gunnerPosition[2]);
        mountTransform.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-turretYaw.get(1.0f)));
        mountTransform.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(turretPitch.get(1.0f)));

        // Zero the trails when they are not being fed (mirrors AirshipEntity.onUpdate's else branch)
        if (world.isRemote && !(isWithinParticleRange() && getEnginePower() > 0.01)) {
            for (Trail trail : trails) {
                trail.add(ZERO_VEC4, ZERO_VEC4, 0.0f);
            }
        }
    }

    @Override
    public void clientFireWeapons(Entity entity) {
        if (isTurretGunner(entity)) {
            turret.clientFire(-1);
        } else {
            super.clientFireWeapons(entity);
        }
    }

    @Override
    public void fireWeapon(int slot, int index, Vec3d direction) {
        if (slot == -1) {
            turret.fire(direction);
        } else {
            super.fireWeapon(slot, index, direction);
        }
    }

    public Entity getTurretGunner() {
        List<Entity> passengers = getPassengers();
        if (passengers.size() >= 2) {
            return passengers.get(1);
        } else {
            return null;
        }
    }

    public boolean isTurretGunner(Entity entity) {
        return getTurretGunner() == entity;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (isTurretGunner(passenger)) {
            Matrix4f transform = getVehicleTransform();

            float[] position = getGunnerPosition();

            Vector4f worldPosition = transformPosition(transform, position[0], (float) (position[1] + passenger.getYOffset()), position[2]);
            passenger.setPosition(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());

            copyEntityData(passenger);
        } else {
            super.updatePassenger(passenger);
        }
    }

    private float[] getGunnerPosition() {
        float pitch = turretPitch.getSmooth(1.0f) / 180.0f * (float) Math.PI;
        float yaw = turretYaw.getSmooth(1.0f) / 180.0f * (float) Math.PI;

        float[] position = new float[]{0.0f, 0.5f, 2.5f};
        boneOffset(position, new float[]{0.0f, -yaw, 0.0f}, new float[]{0.0f, 0.5f, 2.0f});
        boneOffset(position, new float[]{pitch, 0.0f, 0.0f}, new float[]{0.0f, 0.5f, 0.9f});

        return position;
    }

    private void boneOffset(float[] position, float[] rotation, float[] origin) {
        float deg = (float) (180.0 / Math.PI);
        Matrix3f m = Matrix3f.scale(1.0f, 1.0f, 1.0f);
        m.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotation[2] * deg));
        m.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotation[1] * deg));
        m.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(rotation[0] * deg));

        Vec3f v = new Vec3f(position[0] - origin[0], position[1] - origin[1], position[2] - origin[2]);
        v.transform(m);
        position[0] = v.getX() + origin[0];
        position[1] = v.getY() + origin[1];
        position[2] = v.getZ() + origin[2];
    }

    @Override
    protected void copyEntityData(Entity entity) {
        if (isTurretGunner(entity)) {
            if (entity instanceof EntityLivingBase) {
                ((EntityLivingBase) entity).renderYawOffset = getYaw() + turretYaw.getSmooth(1.0f);
            }

            float py = MathHelper.wrapDegrees(entity.rotationYaw - getYaw());
            float cpy = MathHelper.clamp(py, -45f, 45f);
            entity.prevRotationYaw += cpy - py;
            entity.rotationYaw = entity.rotationYaw + cpy - py;
            entity.setRotationYawHead(entity.rotationYaw);

            float pp = MathHelper.wrapDegrees(entity.rotationPitch - getPitch());
            float cpp = MathHelper.clamp(pp, -30f, 30f);
            entity.prevRotationPitch += cpp - pp;
            entity.rotationPitch = entity.rotationPitch + cpp - pp;
        } else {
            super.copyEntityData(entity);
        }
    }
}
