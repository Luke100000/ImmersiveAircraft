package immersive_aircraft.client;

import immersive_aircraft.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class KeyBindings {
    public static List<KeyBinding> list = new LinkedList<>();

    public static final KeyBinding left, right, forward, backward, up, down, pull, push;
    public static final KeyBinding dismount, boost;

    static {
        if (Config.getInstance().useCustomKeybindSystem) {
            left = newMultiKey("control_left", GLFW.GLFW_KEY_A);
            right = newMultiKey("control_right", GLFW.GLFW_KEY_D);
            forward = newMultiKey("control_forward", GLFW.GLFW_KEY_W);
            backward = newMultiKey("control_backward", GLFW.GLFW_KEY_S);
            up = newMultiKey("control_up", GLFW.GLFW_KEY_SPACE);
            down = newMultiKey("control_down", GLFW.GLFW_KEY_LEFT_SHIFT);
            pull = newMultiKey("control_pull", GLFW.GLFW_KEY_S);
            push = newMultiKey("control_push", GLFW.GLFW_KEY_W);

            dismount = newMultiKey("dismount", GLFW.GLFW_KEY_R);
            boost = newMultiKey("boost", GLFW.GLFW_KEY_B);
        } else {
            MinecraftClient client = MinecraftClient.getInstance();

            left = newFallbackKey("fallback_control_left", () -> client.options.leftKey);
            right = newFallbackKey("fallback_control_right", () -> client.options.rightKey);
            forward = newFallbackKey("fallback_control_forward", () -> client.options.forwardKey);
            backward = newFallbackKey("fallback_control_backward", () -> client.options.backKey);
            up = newFallbackKey("fallback_control_up", () -> client.options.jumpKey);
            down = newFallbackKey("fallback_control_down", () -> client.options.sneakKey);
            pull = newFallbackKey("fallback_control_pull", () -> client.options.backKey);
            push = newFallbackKey("fallback_control_push", () -> client.options.forwardKey);

            dismount = newKey("fallback_dismount", GLFW.GLFW_KEY_R);
            boost = newKey("fallback_boost", GLFW.GLFW_KEY_B);
        }
    }

    private static KeyBinding newFallbackKey(String name, Supplier<KeyBinding> fallback) {
        KeyBinding key = new FallbackKeyBinding(
                "key.immersive_aircraft." + name,
                InputUtil.Type.KEYSYM,
                fallback,
                "itemGroup.immersive_aircraft.immersive_aircraft_tab"
        );
        list.add(key);
        return key;
    }

    private static KeyBinding newKey(String name, int code) {
        KeyBinding key = new KeyBinding(
                "key.immersive_aircraft." + name,
                InputUtil.Type.KEYSYM,
                code,
                "itemGroup.immersive_aircraft.immersive_aircraft_tab"
        );
        list.add(key);
        return key;
    }

    private static KeyBinding newMultiKey(String name, int defaultKey) {
        KeyBinding key = new MultiKeyBinding(
                "key.immersive_aircraft." + name,
                InputUtil.Type.KEYSYM,
                defaultKey,
                "itemGroup.immersive_aircraft.immersive_aircraft_tab"
        );
        list.add(key);
        return key;
    }
}
