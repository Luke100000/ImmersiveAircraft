package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Sounds;
import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public class GyrodyneEntity extends Rotorcraft {
    @Override
    public GUI_STYLE getGuiStyle() {
        return GUI_STYLE.NONE;
    }

    public GyrodyneEntity(EntityType<? extends AircraftEntity> entityType, Level world) {
        super(entityType, world, false);
    }

    protected SoundEvent getEngineStartSound() {
        return Sounds.WOOSH.get();
    }

    protected SoundEvent getEngineSound() {
        return Sounds.WOOSH.get();
    }

    @Override
    public Item asItem() {
        return Items.GYRODYNE.get();
    }

    @Override
    protected double getDefaultGravity() {
        return (1.0f - getEnginePower()) * super.getDefaultGravity();
    }

    private void updateEnginePowerTooltip() {
        if (getControllingPassenger() instanceof Player player && player.level().isClientSide() && getFuelUtilization() > 0.0) {
            player.displayClientMessage(Component.translatable("immersive_aircraft.gyrodyne_target", (int) (getEngineTarget() * 100.f + 0.5f)), true);
        }
    }

    @Override
    public String getFuelType() {
        return "fat";
    }

    @Override
    public boolean isFuelLow() {
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
                if (getControllingPassenger() instanceof Player player) {
                    player.displayClientMessage(Component.translatable("immersive_aircraft.gyrodyne_target_reached"), true);
                }
                if (onGround()) {
                    setDeltaMovement(getDeltaMovement().add(0, 0.25f, 0));
                }
            }
        }

        // up and down
        float power = getEnginePower() * getProperties().get(VehicleStat.VERTICAL_SPEED) * pressingInterpolatedY.getSmooth();
        Vector3f f = getTopDirection().mul(power);
        setDeltaMovement(getDeltaMovement().add(f.x, f.y, f.z));

        // get direction
        Vector3f direction = getForwardDirection();

        // speed
        float sin = Mth.sin(getXRot() * ((float) Math.PI / 180));
        float thrust = (float) (Math.pow(getEnginePower(), 2.0) * getProperties().get(VehicleStat.ENGINE_SPEED)) * sin;
        if (onGround() && getEngineTarget() < 1.0) {
            thrust = getProperties().get(VehicleStat.PUSH_SPEED) / (1.0f + (float) getDeltaMovement().length() * 5.0f) * pressingInterpolatedZ.getSmooth() * (pressingInterpolatedZ.getSmooth() > 0.0 ? 1.0f : 0.5f) * getEnginePower();
        }

        // accelerate
        Vector3f f2 = direction.mul(thrust);
        setDeltaMovement(getDeltaMovement().add(f2.x, f2.y, f2.z));
    }

    @Override
    public void tick() {
        super.tick();

        if (getControllingPassenger() instanceof ServerPlayer player) {
            float consumption = getFuelConsumption() * 0.025f;
            player.causeFoodExhaustion(consumption);
        }
    }

    @Override
    public float getFuelUtilization() {
        if (getControllingPassenger() instanceof Player player) {
            if (player.getFoodData().getFoodLevel() > 5) {
                return 1.0f;
            } else {
                return 0.0f;
            }
        }
        return 1.0f;
    }

    @Override
    public double getZoom() {
        return 2.0;
    }
}
