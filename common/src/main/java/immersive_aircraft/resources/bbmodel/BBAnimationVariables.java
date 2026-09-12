package immersive_aircraft.resources.bbmodel;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.mXparser;

import java.util.HashMap;
import java.util.Map;

public class BBAnimationVariables {
    public static final Map<String, Argument> REGISTRY = new HashMap<>();

    public static void register(String name) {
        // Expressions keep references to these objects so their compiled form remains valid.
        REGISTRY.computeIfAbsent(name, key -> new Argument("variable_" + key, 0));
    }

    static {
        // Animation values are floats and do not need mXparser's costly decimal correction passes.
        mXparser.disableUlpRounding();
        mXparser.disableCanonicalRounding();
        mXparser.disableAlmostIntRounding();

        register("time");
        register("engine_rotation");
        register("pressing_interpolated_x");
        register("pressing_interpolated_y");
        register("pressing_interpolated_z");
        register("yaw");
        register("pitch");
        register("roll");
        register("velocity_x");
        register("velocity_y");
        register("velocity_z");
        register("turret_yaw");
        register("turret_pitch");
        register("turret_cooldown");
        register("balloon_pitch");
        register("balloon_roll");
        register("chest");
    }

    public static Argument[] getArgumentArray() {
        return REGISTRY.values().toArray(new Argument[0]);
    }

    public static void set(String name, float value) {
        REGISTRY.get(name).setArgumentValue(value);
    }
}
