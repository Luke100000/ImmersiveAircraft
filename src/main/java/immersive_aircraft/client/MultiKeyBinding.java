package immersive_aircraft.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

/**
 * 1.12.2 port: 1.16 used mixins so that several MultiKeyBindings could share one
 * physical key. Without mixins, the pressed state is read directly from the raw
 * keyboard state each client tick ({@link KeyBindings#update()}), bypassing the
 * vanilla one-binding-per-key map.
 */
public class MultiKeyBinding extends AircraftKeyBinding {
    private boolean pressed;
    private boolean wasPressed;

    public MultiKeyBinding(String translationKey, int code, String category) {
        super(translationKey, code, category);
    }

    public void tick() {
        boolean down;
        if (getKeyCode() < 0) {
            // mouse button (1.12 keyCode = button - 100)
            down = org.lwjgl.input.Mouse.isButtonDown(getKeyCode() + 100);
        } else {
            down = getKeyCode() < 256 && Keyboard.isKeyDown(getKeyCode());
        }
        if (Minecraft.getMinecraft().currentScreen != null) {
            down = false;
        }
        wasPressed = down && !pressed;
        pressed = down;
    }

    @Override
    public boolean isPressed() {
        return pressed;
    }

    @Override
    public boolean wasPressed() {
        return wasPressed;
    }
}
