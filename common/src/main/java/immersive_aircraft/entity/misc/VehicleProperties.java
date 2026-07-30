package immersive_aircraft.entity.misc;

import immersive_aircraft.config.Config;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.item.upgrade.VehicleStat;

import java.util.Map;

public class VehicleProperties {
    private final Map<VehicleStat, Float> baseValues;
    private final InventoryVehicleEntity vehicle;

    public VehicleProperties(Map<VehicleStat, Float> baseValues, InventoryVehicleEntity vehicle) {
        this.baseValues = baseValues;
        this.vehicle = vehicle;
    }

    /**
     * Returns the base stat multiplied by the upgrade value.
     * If the base value is 0, an upgrade has no effect, e.g., a vehicle without fuel consumption will never consume fuel.
     */
    public float get(VehicleStat stat) {
        float value = baseValues.getOrDefault(stat, 0.0f) * vehicle.getTotalUpgrade(stat);
        // Apply global engine speed multiplier
        if (stat == VehicleStat.ENGINE_SPEED) {
            value *= Config.getInstance().globalEngineSpeedMultiplier;
        }
        // Apply durability multiplier (0.5 = twice as fragile)
        if (stat == VehicleStat.DURABILITY) {
            value *= Config.getInstance().durabilityMultiplier;
        }
        return value;
    }

    /**
     * Returns the base stat + the upgrade value, used when absolut changes are needed like the stabilizer.
     */
    public float getAdditive(VehicleStat stat) {
        return baseValues.getOrDefault(stat, 0.0f) + vehicle.getTotalUpgrade(stat) - 1.0f;
    }
}