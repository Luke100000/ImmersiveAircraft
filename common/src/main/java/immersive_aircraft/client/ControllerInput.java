package immersive_aircraft.client;

import net.minecraft.client.KeyMapping;

@FunctionalInterface
public interface ControllerInput {
    /** Returns 0..1 for an active binding, or -1 to use the keyboard/default binding. */
    float strength(KeyMapping key);
}
