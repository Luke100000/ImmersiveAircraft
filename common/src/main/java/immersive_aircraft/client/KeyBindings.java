package immersive_aircraft.client;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

public class KeyBindings {
    public static List<KeyBinding> list = new LinkedList<>();

    public static KeyBinding left = newKey("left", GLFW.GLFW_KEY_A);
    public static KeyBinding right = newKey("right", GLFW.GLFW_KEY_D);
    public static KeyBinding forward = newKey("forward", GLFW.GLFW_KEY_W);
    public static KeyBinding backward = newKey("backward", GLFW.GLFW_KEY_S);
    public static KeyBinding up = newKey("up", GLFW.GLFW_KEY_SPACE);
    public static KeyBinding down = newKey("down", GLFW.GLFW_KEY_LEFT_SHIFT);
    public static KeyBinding pull = newKey("pull", GLFW.GLFW_KEY_S);
    public static KeyBinding push = newKey("push", GLFW.GLFW_KEY_W);
    public static KeyBinding dismount = newKey("dismount", GLFW.GLFW_KEY_R);

    private static KeyBinding newKey(String name, int defaultKey) {
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
