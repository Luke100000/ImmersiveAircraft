package immersive_aircraft.item.upgrade;

import java.util.HashMap;
import java.util.Map;

public class AircraftUpgrade {
    private final Map<AircraftStat, Float> stats = new HashMap<>();

    public AircraftUpgrade set(AircraftStat stat, float value) {
        stats.put(stat, value);
        return this;
    }

    public float get(AircraftStat stat) {
        return stats.getOrDefault(stat, 0.0f);
    }

    public Map<AircraftStat, Float> getAll() {
        return stats;
    }
}
