package immersive_aircraft.resources.bbmodel;

import org.mariuszgromada.math.mxparser.Argument;

import java.util.HashMap;
import java.util.Map;

public class BBAnimationVariables {
    public static Map<String, Argument> REGISTRY = new HashMap<>();

    public static void register(String name) {
        REGISTRY.put(name, new Argument(name, 0));
    }
    
    static {
        register("time");
        register("test");
    }

    public static Argument[] getArgumentArray() {
        return REGISTRY.values().toArray(new Argument[0]);
    }

    public void set(String name, float value) {
        REGISTRY.get(name).setArgumentValue(value);
    }
}
