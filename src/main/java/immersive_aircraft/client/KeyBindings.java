package immersive_aircraft.client;

import immersive_aircraft.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class KeyBindings {
    public static List<AircraftKeyBinding> list = new LinkedList<>();

    public static final AircraftKeyBinding left, right, forward, backward, up, down, pull, push;
    public static final AircraftKeyBinding dismount, boost, use;

    static {
        if (Config.getInstance().useCustomKeybindSystem) {
            left = newMultiKey("multi_control_left", Keyboard.KEY_A);
            right = newMultiKey("multi_control_right", Keyboard.KEY_D);
            forward = newMultiKey("multi_control_forward", Keyboard.KEY_W);
            backward = newMultiKey("multi_control_backward", Keyboard.KEY_S);
            up = newMultiKey("multi_control_up", Keyboard.KEY_SPACE);
            down = newMultiKey("multi_control_down", Keyboard.KEY_LSHIFT);
            pull = newMultiKey("multi_control_pull", Keyboard.KEY_S);
            push = newMultiKey("multi_control_push", Keyboard.KEY_W);

            dismount = newMultiKey("multi_dismount", Keyboard.KEY_R);
            boost = newMultiKey("multi_boost", Keyboard.KEY_B);
            // right mouse button (1.12 keyCode = button - 100)
            use = newMultiKey("multi_use", 1 - 100);
        } else {
            GameSettings settings = Minecraft.getMinecraft().gameSettings;

            left = newFallbackKey("fallback_control_left", () -> settings.keyBindLeft);
            right = newFallbackKey("fallback_control_right", () -> settings.keyBindRight);
            forward = newFallbackKey("fallback_control_forward", () -> settings.keyBindForward);
            backward = newFallbackKey("fallback_control_backward", () -> settings.keyBindBack);
            up = newFallbackKey("fallback_control_up", () -> settings.keyBindJump);
            down = newFallbackKey("fallback_control_down", () -> settings.keyBindSneak);
            pull = newFallbackKey("fallback_control_pull", () -> settings.keyBindBack);
            push = newFallbackKey("fallback_control_push", () -> settings.keyBindForward);

            dismount = newKey("fallback_dismount", Keyboard.KEY_R);
            boost = newKey("fallback_boost", Keyboard.KEY_B);
            use = newFallbackKey("fallback_use", () -> settings.keyBindUseItem);
        }
    }

    private static AircraftKeyBinding newFallbackKey(String name, Supplier<net.minecraft.client.settings.KeyBinding> fallback) {
        AircraftKeyBinding key = new FallbackKeyBinding(
                "key.immersive_aircraft." + name,
                fallback,
                "itemGroup.immersive_aircraft.immersive_aircraft_tab"
        );
        list.add(key);
        return key;
    }

    private static AircraftKeyBinding newKey(String name, int code) {
        AircraftKeyBinding key = new SimpleKeyBinding(
                "key.immersive_aircraft." + name,
                code,
                "itemGroup.immersive_aircraft.immersive_aircraft_tab"
        );
        list.add(key);
        return key;
    }

    private static AircraftKeyBinding newMultiKey(String name, int defaultKey) {
        AircraftKeyBinding key = new MultiKeyBinding(
                "key.immersive_aircraft." + name,
                defaultKey,
                "itemGroup.immersive_aircraft.immersive_aircraft_tab"
        );
        list.add(key);
        return key;
    }

    /**
     * Called from ClientProxy during preInit; registers all bindings with the game.
     */
    public static void bootstrap() {
        for (AircraftKeyBinding key : list) {
            ClientRegistry.registerKeyBinding(key);
        }
    }

    /**
     * Called every client tick; updates the raw key states of the multi bindings.
     */
    public static void update() {
        for (AircraftKeyBinding key : list) {
            if (key instanceof MultiKeyBinding) {
                ((MultiKeyBinding) key).tick();
            }
        }
    }
}
