package immersive_aircraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import immersive_aircraft.Main;
import immersive_aircraft.config.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class KeyBindings {
    public static final List<KeyMapping> list = new LinkedList<>();

    public static final KeyMapping left, right, forward, backward, up, down, pull, push;
    public static final KeyMapping dismount, boost, use;

    static {
        if (Config.getInstance().useCustomKeybindSystem && Main.MOD_LOADER.equals("fabric")) {
            left = newMultiKey("multi_control_left", GLFW.GLFW_KEY_A);
            right = newMultiKey("multi_control_right", GLFW.GLFW_KEY_D);
            forward = newMultiKey("multi_control_forward", GLFW.GLFW_KEY_W);
            backward = newMultiKey("multi_control_backward", GLFW.GLFW_KEY_S);
            up = newMultiKey("multi_control_up", GLFW.GLFW_KEY_SPACE);
            down = newMultiKey("multi_control_down", GLFW.GLFW_KEY_LEFT_SHIFT);
            pull = newMultiKey("multi_control_pull", GLFW.GLFW_KEY_S);
            push = newMultiKey("multi_control_push", GLFW.GLFW_KEY_W);

            use = newMultiKey("multi_use", GLFW.GLFW_MOUSE_BUTTON_2, InputConstants.Type.MOUSE);
        } else {
            Minecraft client = Minecraft.getInstance();

            left = newFallbackKey("fallback_control_left", () -> client.options.keyLeft);
            right = newFallbackKey("fallback_control_right", () -> client.options.keyRight);
            forward = newFallbackKey("fallback_control_forward", () -> client.options.keyUp);
            backward = newFallbackKey("fallback_control_backward", () -> client.options.keyDown);
            up = newFallbackKey("fallback_control_up", () -> client.options.keyJump);
            down = newFallbackKey("fallback_control_down", () -> client.options.keyShift);
            pull = newFallbackKey("fallback_control_pull", () -> client.options.keyDown);
            push = newFallbackKey("fallback_control_push", () -> client.options.keyUp);

            use = newFallbackKey("fallback_use", () -> client.options.keyUse);
        }

        dismount = newKey("dismount", GLFW.GLFW_KEY_R);
        boost = newKey("boost", GLFW.GLFW_KEY_B);
    }

    private static KeyMapping newFallbackKey(String name, Supplier<KeyMapping> fallback) {
        KeyMapping key = new FallbackKeyMapping(
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

    private static KeyMapping newMultiKey(String name, int defaultKey) {
        return newMultiKey(name, defaultKey, InputConstants.Type.KEYSYM);
    }

    private static KeyMapping newMultiKey(String name, int defaultKey, InputConstants.Type type) {
        KeyMapping key = new MultiKeyMapping(
                "key.immersive_aircraft." + name,
                type,
                defaultKey,
                "itemGroup.immersive_aircraft.immersive_aircraft_tab"
        );
        list.add(key);
        return key;
    }
}
