package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.compat.Matrix3f;
import immersive_aircraft.compat.Matrix4f;
import immersive_aircraft.compat.Vec3f;
import immersive_aircraft.compat.Vector4f;
import immersive_aircraft.entity.misc.Trail;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.entity.misc.WeaponMount;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BiplaneEntity extends AirplaneEntity {
    private static final VehicleInventoryDescription inventoryDescription = new VehicleInventoryDescription()
            .addSlot(VehicleInventoryDescription.SlotType.BOILER, 8 + 9, 8 + 22)
            .addSlot(VehicleInventoryDescription.SlotType.BOOSTER, 8 + 9, 8 + 48)
            .addSlot(VehicleInventoryDescription.SlotType.WEAPON, 8 + 18 * 2 + 6, 8 + 6)
            .addSlot(VehicleInventoryDescription.SlotType.BANNER, 8 + 18 * 2 + 28, 8 + 6)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 18 * 2 + 6, 8 + 6 + 22)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 18 * 2 + 28, 8 + 6 + 22)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 18 * 2 + 6, 8 + 6 + 22 * 2)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 18 * 2 + 28, 8 + 6 + 22 * 2)
            .addSlots(VehicleInventoryDescription.SlotType.INVENTORY, 8 + 18 * 5, 8, 4, 4)
            .build();

    @Override
    public VehicleInventoryDescription getInventoryDescription() {
        return inventoryDescription;
    }

    private static final List<Map<WeaponMount.Type, List<WeaponMount>>> WEAPON_MOUNTS = buildWeaponMounts();

    private static List<Map<WeaponMount.Type, List<WeaponMount>>> buildWeaponMounts() {
        List<Map<WeaponMount.Type, List<WeaponMount>>> list = new ArrayList<>();
        Map<WeaponMount.Type, List<WeaponMount>> slot0 = new EnumMap<>(WeaponMount.Type.class);
        slot0.put(WeaponMount.Type.ROTATING, Collections.singletonList(WeaponMount.of(0, 0.9f, -1.1875f, 0, 0, 0)));
        slot0.put(WeaponMount.Type.FRONT, Arrays.asList(WeaponMount.of(-1.0f, 0.3375f, 1.125f, 0, 0, 0), WeaponMount.of(1.0f, 0.3375f, 1.125f, 0, 0, 0)));
        slot0.put(WeaponMount.Type.DROP, Arrays.asList(WeaponMount.of(-1.25f, 0.3f, 1.125f, 0, 0, 0), WeaponMount.of(1.25f, 0.3f, 1.125f, 0, 0, 0)));
        list.add(slot0);
        return list;
    }

    @Override
    protected List<Map<WeaponMount.Type, List<WeaponMount>>> getWeaponMountDefinitions() {
        return WEAPON_MOUNTS;
    }

    public BiplaneEntity(World world) {
        super(world);
        // 1.16 EntityType dimensions: 1.75 x 0.85, fire immune
        setSize(1.75f, 0.85f);
        isImmuneToFire = true;
    }

    @Override
    protected float getBaseFuelConsumption() {
        return 1.25f;
    }

    final List<List<Vec3d>> PASSENGER_POSITIONS = Collections.singletonList(Collections.singletonList(new Vec3d(0.0f, 0.05f, -0.6f)));

    protected List<List<Vec3d>> getPassengerPositions() {
        return PASSENGER_POSITIONS;
    }

    private final List<Trail> trails = Arrays.asList(new Trail(40), new Trail(40));

    public List<Trail> getTrails() {
        return trails;
    }

    private void trail(Matrix4f transform, int index, float x, float y, float z) {
        Vector4f p0 = transformPosition(transform, x, y - 0.15f, z);
        Vector4f p1 = transformPosition(transform, x, y + 0.15f, z);

        float trailStrength = Math.max(0.0f, Math.min(1.0f, (float)(Math.sqrt(getVelocity().length()) * (0.5f + (pressingInterpolatedX.getSmooth() * x) * 0.025f) - 0.25f)));
        trails.get(index).add(p0, p1, trailStrength);
    }

    @Override
    public Item asItem() {
        return Items.BIPLANE.get();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (world.isRemote) {
            if (isWithinParticleRange()) {
                Matrix4f transform = getVehicleTransform();
                Matrix3f normalTransform = getVehicleNormalTransform();

                // Trails
                trail(transform, 0, -3.75f, 0.25f, 0.6f);
                trail(transform, 1, 3.75f, 0.25f, 0.6f);

                // Smoke
                float power = getEnginePower();
                if (power > 0.05) {
                    Vector4f p = transformPosition(transform, 0.325f * (ticksExisted % 4 == 0 ? -1.0f : 1.0f), 0.5f, 0.8f);
                    Vec3f vel = transformVector(normalTransform, 0.2f * (ticksExisted % 4 == 0 ? -1.0f : 1.0f), 0.0f, 0.0f);
                    Vec3d velocity = getVelocity();
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, p.getX(), p.getY(), p.getZ(), vel.getX() + velocity.x, vel.getY() + velocity.y, vel.getZ() + velocity.z);
                }
            } else {
                trails.get(0).add(ZERO_VEC4, ZERO_VEC4, 0.0f);
                trails.get(1).add(ZERO_VEC4, ZERO_VEC4, 0.0f);
            }
        }
    }
}
