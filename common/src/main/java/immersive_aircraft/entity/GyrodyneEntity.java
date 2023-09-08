package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.entity.misc.AircraftProperties;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.joml.Vector3f;

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

    @Override
    public GUI_STYLE getGuiStyle() {
        return GUI_STYLE.NONE;
    }

    public GyrodyneEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world, false);
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

    final List<List<Vector3f>> PASSENGER_POSITIONS = List.of(
            List.of(
                    new Vector3f(0.0f, -0.1f, 0.3f)
            ),
            List.of(
                    new Vector3f(0.0f, -0.1f, 0.3f),
                    new Vector3f(0.0f, -0.1f, -0.6f)
            )
    );

    protected List<List<Vector3f>> getPassengerPositions() {
        return PASSENGER_POSITIONS;
    }

    @Override
    protected float getGravity() {
        return (1.0f - getEnginePower()) * super.getGravity();
    }

    private void updateEnginePowerTooltip() {
        if (getControllingPassenger() instanceof ClientPlayerEntity player && getFuelUtilization() > 0.0) {
            player.sendMessage(Text.translatable("immersive_aircraft.gyrodyne_target", (int)(getEngineTarget() * 100.f + 0.5f)), true);
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
                if (getControllingPassenger() instanceof ClientPlayerEntity player) {
                    player.sendMessage(Text.translatable("immersive_aircraft.gyrodyne_target_reached"), true);
                    if (isOnGround()) {
                        setVelocity(getVelocity().add(0, 0.25f, 0));
                    }
                }
            }
        }

        // up and down
        float power = getEnginePower() * properties.getVerticalSpeed() * pressingInterpolatedY.getSmooth();
        Vector3f f = getTopDirection().mul(power);
        setVelocity(getVelocity().add(f.x, f.y, f.z));

        // get direction
        Vector3f direction = getDirection();

        // speed
        float sin = MathHelper.sin(getPitch() * ((float)Math.PI / 180));
        float thrust = (float)(Math.pow(getEnginePower(), 2.0) * properties.getEngineSpeed()) * sin;
        if (isOnGround() && getEngineTarget() < 1.0) {
            thrust = PUSH_SPEED / (1.0f + (float)getVelocity().length() * 5.0f) * pressingInterpolatedZ.getSmooth() * (pressingInterpolatedZ.getSmooth() > 0.0 ? 1.0f : 0.5f) * getEnginePower();
        }

        // accelerate
        Vector3f f2 = direction.mul(thrust);
        setVelocity(getVelocity().add(f2.x, f2.y, f2.z));
    }

    @Override
    public void tick() {
        super.tick();

        if (getControllingPassenger() instanceof ServerPlayerEntity player) {
            float consumption = getFuelConsumption() * 0.025f;
            player.getHungerManager().addExhaustion(consumption);
        }
    }

    @Override
    public float getFuelUtilization() {
        if (getControllingPassenger() instanceof PlayerEntity player && player.getHungerManager().getFoodLevel() > 5) {
            return 1.0f;
        }
        return 0.0f;
    }
}
