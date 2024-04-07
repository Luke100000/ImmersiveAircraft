package immersive_aircraft.item.upgrade;

import java.util.HashMap;
import java.util.Map;

public class VehicleUpgrade {
    private final Map<VehicleStat, Float> stats = new HashMap<>();

    public void set(VehicleStat stat, float value) {
        stats.put(stat, value);
    }

    public float get(VehicleStat stat) {
        return stats.getOrDefault(stat, 0.0f);
    }

    public Map<VehicleStat, Float> getAll() {
        return stats;
    }
}
