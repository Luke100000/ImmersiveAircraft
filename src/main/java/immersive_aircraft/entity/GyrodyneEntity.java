package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.entity.misc.AircraftProperties;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.entity.misc.WeaponMount;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Collections;
import java.util.List;

public class GyrodyneEntity extends Rotorcraft {
    private final static float PUSH_SPEED = 0.25f;

    private final AircraftProperties properties = new AircraftProperties(this)
            .setYawSpeed(5.0f)
            .setPitchSpeed(5.0f)
            .setEngineSpeed(0.3f)
            .setVerticalSpeed(0.04f)
            .setDriftDrag(0.01f)
            .setLift(0.1f)
            .setRollFactor(30.0f)
            .setWindSensitivity(0.05f)
            .setMass(4.0f);

    private static final VehicleInventoryDescription inventoryDescription = new VehicleInventoryDescription()
            .addSlot(VehicleInventoryDescription.SlotType.WEAPON, 8 + 6, 8 + 6)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 28, 8 + 6)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 6, 8 + 6 + 22)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 28, 8 + 6 + 22)
            .addSlots(VehicleInventoryDescription.SlotType.INVENTORY, 8 + 18 * 3, 8, 6, 3)
            .build();

    @Override
    public VehicleInventoryDescription getInventoryDescription() {
        return inventoryDescription;
    }

    private static final List<Map<WeaponMount.Type, List<WeaponMount>>> WEAPON_MOUNTS = buildWeaponMounts();

    private static List<Map<WeaponMount.Type, List<WeaponMount>>> buildWeaponMounts() {
        List<Map<WeaponMount.Type, List<WeaponMount>>> list = new ArrayList<>();
        Map<WeaponMount.Type, List<WeaponMount>> slot0 = new EnumMap<>(WeaponMount.Type.class);
        slot0.put(WeaponMount.Type.ROTATING, Collections.singletonList(WeaponMount.of(0, 0.075f, -1.0f, 0, 0, 0)));
        slot0.put(WeaponMount.Type.FRONT, Collections.singletonList(WeaponMount.of(0, 0.2f, 1.375f, 0, 0, 0)));
        slot0.put(WeaponMount.Type.DROP, Arrays.asList(WeaponMount.of(-1.0f, 0.23125f, 0.0625f, 0, 0, 0), WeaponMount.of(1.0f, 0.23125f, 0.0625f, 0, 0, 0)));
        list.add(slot0);
        return list;
    }

    @Override
    protected List<Map<WeaponMount.Type, List<WeaponMount>>> getWeaponMountDefinitions() {
        return WEAPON_MOUNTS;
    }

    @Override
    public GUI_STYLE getGuiStyle() {
        return GUI_STYLE.NONE;
    }

    public GyrodyneEntity(World world) {
        super(world);
        // 1.16 EntityType dimensions: 1.25 x 0.6, fire immune
        setSize(1.25f, 0.6f);
        isImmuneToFire = true;
    }

    protected SoundEvent getEngineStartSound() {
        return Sounds.WOOSH.get();
    }

    protected SoundEvent getEngineSound() {
        return Sounds.WOOSH.get();
    }

    @Override
    protected float getStabilizer() {
        return 0.3f;
    }

    @Override
    public AircraftProperties getProperties() {
        return properties;
    }

    @Override
    protected float getGroundVelocityDecay() {
        return falloffGroundVelocityDecay(0.8f);
    }

    @Override
    protected float getHorizontalVelocityDelay() {
        return 0.925f;
    }

    @Override
    protected float getVerticalVelocityDelay() {
        return 0.9f;
    }

    @Override
    public Item asItem() {
        return Items.GYRODYNE.get();
    }

    final List<List<Vec3d>> PASSENGER_POSITIONS = Arrays.asList(
            Collections.singletonList(
                    new Vec3d(0.0f, -0.1f, 0.3f)
            ),
            Arrays.asList(
                    new Vec3d(0.0f, -0.1f, 0.3f),
                    new Vec3d(0.0f, -0.1f, -0.6f)
            )
    );

    protected List<List<Vec3d>> getPassengerPositions() {
        return PASSENGER_POSITIONS;
    }

    @Override
    protected float getGravity() {
        return (1.0f - getEnginePower()) * super.getGravity();
    }

    private void updateEnginePowerTooltip() {
        if (getControllingPassenger() instanceof EntityPlayerSP && getFuelUtilization() > 0.0) {
            EntityPlayerSP player = (EntityPlayerSP)getControllingPassenger();
            player.sendStatusMessage(new TextComponentTranslation("immersive_aircraft.gyrodyne_target", (int)(getEngineTarget() * 100.f + 0.5f)), true);
        }
    }

    @Override
    protected String getFuelType() {
        return "fat";
    }

    @Override
    protected boolean isFuelLow() {
        return false;
    }

    @Override
    protected void updateController() {
        super.updateController();

        // launch that engine
        if (getEngineTarget() < 1.0f) {
            setEngineTarget(Math.max(0.0f, Math.min(1.0f, getEngineTarget() + pressingInterpolatedZ.getValue() * 0.05f - 0.035f)));
            updateEnginePowerTooltip();

            if (getEngineTarget() == 1.0) {
                if (getControllingPassenger() instanceof EntityPlayerSP) {
                    EntityPlayerSP player = (EntityPlayerSP)getControllingPassenger();
                    player.sendStatusMessage(new TextComponentTranslation("immersive_aircraft.gyrodyne_target_reached"), true);
                    if (onGround) {
                        setVelocity(getVelocity().add(0, 0.25f, 0));
                    }
                }
            }
        }

        // up and down
        float power = getEnginePower() * properties.getVerticalSpeed() * pressingInterpolatedY.getSmooth();
        setVelocity(getVelocity().add(getTopDirection().scale(power)));

        // get direction
        Vec3d direction = getDirection();

        // speed
        float sin = MathHelper.sin(getPitch() * ((float)Math.PI / 180));
        float thrust = (float)(Math.pow(getEnginePower(), 2.0) * properties.getEngineSpeed()) * sin;
        if (onGround && getEngineTarget() < 1.0) {
            thrust = PUSH_SPEED / (1.0f + (float)getVelocity().length() * 5.0f) * pressingInterpolatedZ.getSmooth() * (pressingInterpolatedZ.getSmooth() > 0.0 ? 1.0f : 0.5f) * getEnginePower();
        }

        // accelerate
        setVelocity(getVelocity().add(direction.scale(thrust)));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (getControllingPassenger() instanceof EntityPlayerMP) {
            float consumption = getFuelConsumption() * 0.025f;
            ((EntityPlayerMP)getControllingPassenger()).getFoodStats().addExhaustion(consumption);
        }
    }

    @Override
    public float getFuelUtilization() {
        if (getControllingPassenger() instanceof EntityPlayer && ((EntityPlayer)getControllingPassenger()).getFoodStats().getFoodLevel() > 5) {
            return 1.0f;
        }
        return 0.0f;
    }
}
