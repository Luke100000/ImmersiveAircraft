package immersive_aircraft.client;

import net.minecraft.client.settings.KeyBinding;

import java.util.function.Supplier;

public class FallbackKeyBinding extends AircraftKeyBinding {
    public Supplier<KeyBinding> fallbackKey;

    public FallbackKeyBinding(String translationKey, Supplier<KeyBinding> fallbackKey, String category) {
        super(translationKey, 0, category);

        this.fallbackKey = fallbackKey;
    }

    @Override
    public boolean isPressed() {
        if (getKeyCode() == 0) {
            return fallbackKey.get().isKeyDown();
        } else {
            return superIsKeyDown();
        }
    }

    @Override
    public boolean wasPressed() {
        if (getKeyCode() == 0) {
            return fallbackKey.get().isPressed();
        } else {
            return superIsPressed();
        }
    }
}
