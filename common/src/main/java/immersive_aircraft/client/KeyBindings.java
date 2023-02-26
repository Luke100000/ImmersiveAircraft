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

    public static KeyBinding left = newFallbackKey("left", () -> MinecraftClient.getInstance().options.leftKey);
    public static KeyBinding right = newFallbackKey("right", () -> MinecraftClient.getInstance().options.rightKey);
    public static KeyBinding forward = newFallbackKey("forward", () -> MinecraftClient.getInstance().options.forwardKey);
    public static KeyBinding backward = newFallbackKey("backward", () -> MinecraftClient.getInstance().options.backKey);
    public static KeyBinding up = newFallbackKey("up", () -> MinecraftClient.getInstance().options.jumpKey);
    public static KeyBinding down = newFallbackKey("down", () -> MinecraftClient.getInstance().options.sneakKey);
    public static KeyBinding pull = newFallbackKey("pull", () -> MinecraftClient.getInstance().options.backKey);
    public static KeyBinding push = newFallbackKey("push", () -> MinecraftClient.getInstance().options.forwardKey);

    public static KeyBinding dismount = newKey("dismount", GLFW.GLFW_KEY_R);

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
