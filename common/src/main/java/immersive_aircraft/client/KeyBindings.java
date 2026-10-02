package immersive_aircraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import immersive_aircraft.Main;
import immersive_aircraft.config.Config;
import immersive_aircraft.mixin.client.KeyMappingAccessorMixin;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class KeyBindings {
    public static final List<KeyMapping> list = new LinkedList<>();
    private static final Set<InputConstants.Key> physicalKeys = new HashSet<>();
    private static final boolean useMultiKeys = Config.getInstance().useCustomKeybindSystem && Main.MOD_LOADER.equals("fabric");

    public static final KeyMapping left, right, forward, backward, up, down, pull, push;
    public static final KeyMapping dismount, boost, use;

    static {
        Minecraft client = Minecraft.getInstance();
        left = newControlKey("control_left", GLFW.GLFW_KEY_A, () -> client.options.keyLeft);
        right = newControlKey("control_right", GLFW.GLFW_KEY_D, () -> client.options.keyRight);
        forward = newControlKey("control_forward", GLFW.GLFW_KEY_W, () -> client.options.keyUp);
        backward = newControlKey("control_backward", GLFW.GLFW_KEY_S, () -> client.options.keyDown);
        up = newControlKey("control_up", GLFW.GLFW_KEY_SPACE, () -> client.options.keyJump);
        down = newControlKey("control_down", GLFW.GLFW_KEY_LEFT_SHIFT, () -> client.options.keyShift);
        pull = newControlKey("control_pull", GLFW.GLFW_KEY_S, () -> client.options.keyDown);
        push = newControlKey("control_push", GLFW.GLFW_KEY_W, () -> client.options.keyUp);
        use = newControlKey("use", GLFW.GLFW_MOUSE_BUTTON_2, InputConstants.Type.MOUSE, () -> client.options.keyUse);

        dismount = newKey("dismount", GLFW.GLFW_KEY_R);
        boost = newKey("boost", GLFW.GLFW_KEY_B);
    }

    private static KeyMapping newControlKey(String name, int defaultKey, Supplier<KeyMapping> fallback) {
        return newControlKey(name, defaultKey, InputConstants.Type.KEYSYM, fallback);
    }

    private static KeyMapping newControlKey(String name, int defaultKey, InputConstants.Type type, Supplier<KeyMapping> fallback) {
        String translationKey = "key.immersive_aircraft." + (useMultiKeys ? "multi_" : "fallback_") + name;
        String category = "itemGroup.immersive_aircraft.immersive_aircraft_tab";
        KeyMapping key = useMultiKeys
                ? new MultiKeyMapping(translationKey, type, defaultKey, fallback, category)
                : new FallbackKeyMapping(translationKey, InputConstants.Type.KEYSYM, fallback, category);
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

    public static InputConstants.Key getBoundKey(KeyMapping key) {
        if (key instanceof MultiKeyMapping multi) {
            return multi.getBoundKey();
        }
        if (key instanceof FallbackKeyMapping fallback) {
            return fallback.getBoundKey();
        }
        return ((KeyMappingAccessorMixin) key).getKey();
    }

    public static KeyMapping getFallbackKey(KeyMapping key) {
        if (key instanceof MultiKeyMapping multi) {
            return multi.getFallbackKey();
        }
        if (key instanceof FallbackKeyMapping fallback) {
            return fallback.getFallbackKey();
        }
        return null;
    }

    public static boolean isPhysicalDown(KeyMapping key) {
        return physicalKeys.contains(getBoundKey(key));
    }

    public static void setPhysicalKey(InputConstants.Key key, boolean pressed) {
        if (pressed) {
            physicalKeys.add(key);
        } else {
            physicalKeys.remove(key);
        }
    }

    public static void clearPhysicalKeys() {
        physicalKeys.clear();
    }

    public static void refreshPhysicalKeys(Iterable<KeyMapping> bindings) {
        long window = Minecraft.getInstance().getWindow().getWindow();
        for (KeyMapping binding : bindings) {
            InputConstants.Key key = getBoundKey(binding);
            if (key.getType() == InputConstants.Type.KEYSYM && !key.equals(InputConstants.UNKNOWN)) {
                boolean pressed = InputConstants.isKeyDown(window, key.getValue());
                setPhysicalKey(key, pressed);
                if (binding instanceof MultiKeyMapping) {
                    binding.setDown(pressed);
                }
            }
        }
    }
}
