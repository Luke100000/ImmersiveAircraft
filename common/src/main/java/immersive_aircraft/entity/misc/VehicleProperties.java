package immersive_aircraft.entity.misc;

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
        return baseValues.getOrDefault(stat, 0.0f) * vehicle.getTotalUpgrade(stat);
    }

    /**
     * Returns the base stat + the upgrade value, used when absolut changes are needed like the stabilizer.
     */
    public float getAdditive(VehicleStat stat) {
        return baseValues.getOrDefault(stat, 0.0f) + vehicle.getTotalUpgrade(stat) - 1.0f;
    }
}
