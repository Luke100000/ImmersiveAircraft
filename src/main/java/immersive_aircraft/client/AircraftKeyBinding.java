package immersive_aircraft.client;

import net.minecraft.client.settings.KeyBinding;

/**
 * Base class for the mod key bindings exposing the 1.16 Yarn call shape:
 * {@link #isPressed()} is the held state (1.12: isKeyDown), {@link #wasPressed()}
 * is edge-triggered (1.12: isPressed).
 */
public abstract class AircraftKeyBinding extends KeyBinding {
    public AircraftKeyBinding(String description, int keyCode, String category) {
        super(description, keyCode, category);
    }

    /**
     * Held state (Yarn isPressed).
     */
    public abstract boolean isPressed();

    /**
     * Edge-triggered (Yarn wasPressed).
     */
    public abstract boolean wasPressed();

    protected boolean superIsKeyDown() {
        return super.isKeyDown();
    }

    protected boolean superIsPressed() {
        return super.isPressed();
    }
}
