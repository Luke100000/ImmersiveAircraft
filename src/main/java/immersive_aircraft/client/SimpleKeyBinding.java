package immersive_aircraft.client;

/**
 * Plain key binding adapting the 1.12 semantics to the Yarn call shape.
 */
public class SimpleKeyBinding extends AircraftKeyBinding {
    public SimpleKeyBinding(String translationKey, int code, String category) {
        super(translationKey, code, category);
    }

    @Override
    public boolean isPressed() {
        return superIsKeyDown();
    }

    @Override
    public boolean wasPressed() {
        return superIsPressed();
    }
}
