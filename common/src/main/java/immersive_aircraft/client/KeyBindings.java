package immersive_aircraft.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class KeyBindings {
    public static List<KeyBinding> list = new LinkedList<>();

    public static KeyBinding left = newFallbackKey("control_left", () -> MinecraftClient.getInstance().options.leftKey);
    public static KeyBinding right = newFallbackKey("control_right", () -> MinecraftClient.getInstance().options.rightKey);
    public static KeyBinding forward = newFallbackKey("control_forward", () -> MinecraftClient.getInstance().options.forwardKey);
    public static KeyBinding backward = newFallbackKey("control_backward", () -> MinecraftClient.getInstance().options.backKey);
    public static KeyBinding up = newFallbackKey("control_up", () -> MinecraftClient.getInstance().options.jumpKey);
    public static KeyBinding down = newFallbackKey("control_down", () -> MinecraftClient.getInstance().options.sneakKey);
    public static KeyBinding pull = newFallbackKey("control_pull", () -> MinecraftClient.getInstance().options.backKey);
    public static KeyBinding push = newFallbackKey("control_push", () -> MinecraftClient.getInstance().options.forwardKey);

    public static KeyBinding dismount = newKey("dismount", GLFW.GLFW_KEY_R);
    public static KeyBinding boost = newKey("boost", GLFW.GLFW_KEY_B);

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
}
