package immersive_aircraft.client;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class KeyBindings {
    public static final List<KeyMapping> list = new LinkedList<>();

    public static final KeyMapping left, right, forward, backward, up, down;
    public static final KeyMapping dismount, boost;

    static {
        Minecraft client = Minecraft.getInstance();

        left = newFallbackKey("fallback_control_left", () -> client.options.keyLeft);
        right = newFallbackKey("fallback_control_right", () -> client.options.keyRight);
        forward = newFallbackKey("fallback_control_forward", () -> client.options.keyUp);
        backward = newFallbackKey("fallback_control_backward", () -> client.options.keyDown);
        up = newFallbackKey("fallback_control_up", () -> client.options.keyJump);
        down = newFallbackKey("fallback_control_down", () -> client.options.keyShift);

        dismount = newKey("fallback_dismount", GLFW.GLFW_KEY_R);
        boost = newKey("fallback_boost", GLFW.GLFW_KEY_B);
    }

    private static KeyMapping newFallbackKey(String name, Supplier<KeyMapping> fallback) {
        KeyMapping key = new FallbackKeyBinding(
                "key.immersive_aircraft." + name,
                InputConstants.Type.KEYSYM,
                fallback,
                "itemGroup.immersive_aircraft.immersive_aircraft_tab"
        );
        list.add(key);
        return key;
    }

    private static KeyMapping newKey(String name, int code) {
        KeyMapping key = new KeyMapping(
                "key.immersive_aircraft." + name,
                InputConstants.Type.KEYSYM,
                code,
                "itemGroup.immersive_aircraft.immersive_aircraft_tab"
        );
        list.add(key);
        return key;
    }
}
