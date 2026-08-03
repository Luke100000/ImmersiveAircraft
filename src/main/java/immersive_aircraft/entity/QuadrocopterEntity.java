package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.entity.misc.AircraftProperties;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.entity.misc.WeaponMount;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;

public class QuadrocopterEntity extends Rotorcraft {
    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(5.0f)
            .setPitchSpeed(1.5f)
            .setEngineSpeed(0.0325f)
            .setVerticalSpeed(0.0325f)
            .setGlideFactor(0.0f)
            .setDriftDrag(0.005f)
            .setLift(0.1f)
            .setRollFactor(15.0f)
            .setWindSensitivity(0.0125f)
            .setMass(1.0f);

    private static final VehicleInventoryDescription inventoryDescription = new VehicleInventoryDescription()
            .addSlot(VehicleInventoryDescription.SlotType.BOILER, 8 + 9, 8 + 14)
            .addSlot(VehicleInventoryDescription.SlotType.WEAPON, 8 + 18 * 2 + 6, 8 + 6)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 18 * 2 + 6 + 22, 8 + 6)
            .addSlots(VehicleInventoryDescription.SlotType.INVENTORY, 8 + 18 * 5, 8, 3, 2)
            .build();

    @Override
    public VehicleInventoryDescription getInventoryDescription() {
        return inventoryDescription;
    }

    private static final List<Map<WeaponMount.Type, List<WeaponMount>>> WEAPON_MOUNTS = buildWeaponMounts();

    private static List<Map<WeaponMount.Type, List<WeaponMount>>> buildWeaponMounts() {
        List<Map<WeaponMount.Type, List<WeaponMount>>> list = new ArrayList<>();
        Map<WeaponMount.Type, List<WeaponMount>> slot0 = new EnumMap<>(WeaponMount.Type.class);
        slot0.put(WeaponMount.Type.ROTATING, Collections.singletonList(WeaponMount.of(0, 0.875f, -0.5f, 0, 0, 0, true)));
        slot0.put(WeaponMount.Type.FRONT, Collections.singletonList(WeaponMount.of(0, 0.375f, 1.0f, 0, 0, 0)));
        slot0.put(WeaponMount.Type.DROP, Collections.singletonList(WeaponMount.of(0, 0.375f, 0, 0, 0, 0)));
        list.add(slot0);
        return list;
    }

    @Override
    protected List<Map<WeaponMount.Type, List<WeaponMount>>> getWeaponMountDefinitions() {
        return WEAPON_MOUNTS;
    }

    public QuadrocopterEntity(World world) {
        super(world);
        // 1.16 EntityType dimensions: 1.5 x 0.5, fire immune
        setSize(1.5f, 0.5f);
        isImmuneToFire = true;
    }

    @Override
    protected float getBaseFuelConsumption() {
        return 0.5f;
    }

    @Override
    protected float getInputInterpolationSteps() {
        return 5;
    }

    protected SoundEvent getEngineSound() {
        return Sounds.PROPELLER_TINY.get();
    }

    @Override
    protected float getEnginePitch() {
        return 1.0f;
    }

    @Override
    public AircraftProperties getProperties() {
        return properties;
    }

    @Override
    protected float getGroundVelocityDecay() {
        return 0.25f;
    }

    @Override
    protected float getHorizontalVelocityDelay() {
        return 0.9f;
    }

    @Override
    protected float getVerticalVelocityDelay() {
        return 0.8f;
    }

    @Override
    protected float getStabilizer() {
        return 0.1f;
    }

    @Override
    public Item asItem() {
        return Items.QUADROCOPTER.get();
    }

    final List<List<Vec3d>> PASSENGER_POSITIONS = Collections.singletonList(
            Collections.singletonList(
                    new Vec3d(0.0f, 0.275f, -0.1f)
            )
    );

    protected List<List<Vec3d>> getPassengerPositions() {
        return PASSENGER_POSITIONS;
    }

    @Override
    protected float getGravity() {
        return inWater ? 0.04f : (1.0f - getEnginePower()) * super.getGravity();
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
        float thrust = (float)(Math.pow(getEnginePower(), 5.0) * properties.getEngineSpeed()) * pressingInterpolatedZ.getSmooth();
        setVelocity(getVelocity().add(direction.scale(thrust)));
    }
}
