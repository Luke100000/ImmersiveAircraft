package immersive_aircraft.item.upgrade;

import java.util.HashMap;
import java.util.Map;

public record VehicleStat(String name, boolean positive, float defaultValue) {
    public static final Map<String, VehicleStat> STATS = new HashMap<>();

    public static final VehicleStat ENGINE_SPEED = register("engineSpeed", true);
    public static final VehicleStat VERTICAL_SPEED = register("verticalSpeed", true);
    public static final VehicleStat YAW_SPEED = register("yawSpeed", true);
    public static final VehicleStat PITCH_SPEED = register("pitchSpeed", true);
    public static final VehicleStat PUSH_SPEED = register("pushSpeed", true);

    public static final VehicleStat ACCELERATION = register("acceleration", true, 1.0f);
    public static final VehicleStat DURABILITY = register("durability", true, 1.0f);
    public static final VehicleStat FUEL = register("fuel", false, 1.0f);

    public static final VehicleStat FRICTION = register("friction", false, 0.015f);
    public static final VehicleStat GLIDE_FACTOR = register("glideFactor", true);
    public static final VehicleStat LIFT = register("lift", true);

    public static final VehicleStat ROLL_FACTOR = register("rollFactor", true);
    public static final VehicleStat GROUND_PITCH = register("groundPitch", true);
    public static final VehicleStat STABILIZER = register("stabilizer", true, 0.0f);

    public static final VehicleStat WIND = register("wind", false);
    public static final VehicleStat MASS = register("mass", false, 1.0f);

    public static final VehicleStat HUD = register("hud", false, -1.0f);
    public static final VehicleStat DIALS = register("dials", false, -1.0f);

    public static final VehicleStat GROUND_FRICTION = register("groundFriction", false, 0.95f);
    public static final VehicleStat WATER_FRICTION = register("waterFriction", false, 0.9f);
    public static final VehicleStat ROTATION_DECAY = register("rotationDecay", false, 0.97f);
    public static final VehicleStat HORIZONTAL_DECAY = register("horizontalDecay", false, 0.97f);
    public static final VehicleStat VERTICAL_DECAY = register("verticalDecay", false, 0.97f);

    public static VehicleStat register(String name, boolean positive) {
        return register(name, positive, 0.0f);
    }

    /**
     * @param name Name of stat as used in vehicle data files
     * @param positive Is higher better?
     * @param defaultValue Default value when not explicitly set in vehicle data file
     * @return A stat reference to be used when querying
     */
    public static VehicleStat register(String name, boolean positive, float defaultValue) {
        VehicleStat stat = new VehicleStat(name, positive, defaultValue);
        STATS.put(name, stat);
        return stat;
    }
}
