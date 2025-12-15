package immersive_aircraft.resources.bbmodel;

import org.mariuszgromada.math.mxparser.Argument;

import java.util.HashMap;
import java.util.Map;

public class BBAnimationVariables {
    private final Map<String, Argument> REGISTRY = new HashMap<>();

    public BBAnimationVariables() {
        AnimationVariableName.getAllNames().forEach(this::register);
    }

    private void register(String name) {
        REGISTRY.put(name, new Argument("variable_" + name, 0));
    }

    public Argument[] getArgumentArray() {
        return REGISTRY.values().toArray(new Argument[0]);
    }

    public void set(AnimationVariableName name, float value) {
        REGISTRY.get(name.getName()).setArgumentValue(value);
    }
}
