package immersive_aircraft.item.upgrade;

import java.util.HashMap;
import java.util.Map;

public record AircraftStat(String name, boolean positive, float defaultValue) {
    public static final Map<String, AircraftStat> STATS = new HashMap<>();

    // Common external factors
    public static final AircraftStat FRICTION = register("friction", true);
    public static final AircraftStat ACCELERATION = register("acceleration", true);
    public static final AircraftStat DURABILITY = register("durability", true, 1.0f);
    public static final AircraftStat FUEL = register("fuel", true);
    public static final AircraftStat WIND = register("wind", true);

    // Usually internal factors
    public static final AircraftStat YAW_SPEED = register("yawSpeed", true);
    public static final AircraftStat PITCH_SPEED = register("pitchSpeed", true);
    public static final AircraftStat ENGINE_SPEED = register("engineSpeed", true);
    public static final AircraftStat VERTICAL_SPEED = register("verticalSpeed", true);
    public static final AircraftStat GLIDE_FACTOR = register("glideFactor", true);
    public static final AircraftStat LIFT = register("lift", true);
    public static final AircraftStat ROLL_FACTOR = register("rollFactor", true);
    public static final AircraftStat GROUND_PITCH = register("groundPitch", true);
    public static final AircraftStat MASS = register("mass", true, 1.0f);

    public static AircraftStat register(String name, boolean positive) {
        return register(name, positive, 0.0f);
    }

    public static AircraftStat register(String name, boolean positive, float defaultValue) {
        AircraftStat stat = new AircraftStat(name, positive, defaultValue);
        STATS.put(name, stat);
        return stat;
    }
}
