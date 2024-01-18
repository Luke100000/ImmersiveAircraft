package immersive_aircraft.entity.misc;

import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.item.upgrade.AircraftStat;

import java.util.Map;

public class AircraftProperties {
    // todo this could replace the base upgrades
    private final Map<AircraftStat, Float> baseValues;
    private final InventoryVehicleEntity vehicle;

    public AircraftProperties(Map<AircraftStat, Float> baseValues, InventoryVehicleEntity vehicle) {
        this.baseValues = baseValues;
        this.vehicle = vehicle;
    }

    public float get(AircraftStat stat) {
        return baseValues.getOrDefault(stat, 0.0f) * vehicle.getTotalUpgrade(stat);
    }
}
