package immersive_aircraft.client;

import immersive_aircraft.CompatUtil;
import immersive_aircraft.client.compat.ControllableInput;
import immersive_aircraft.client.compat.ControlifyInput;
import immersive_aircraft.client.compat.MidnightControlsInput;
import immersive_aircraft.entity.AirplaneEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class AircraftInput {
    private static ControllerInput controller = key -> -1;
    private static boolean initialized;

    public static void registerDefaults() {
        if (CompatUtil.isModLoaded("midnightcontrols")) {
            MidnightControlsInput.registerDefaults();
        }
        if (CompatUtil.isModLoaded("controllable")) {
            ControllableInput.registerDefaults();
        }
    }

    public static void init() {
        // Saved controller mappings must load before adding the dismount default.
        if (initialized || !Minecraft.getInstance().isGameLoadFinished()) return;
        initialized = true;
        if (CompatUtil.isModLoaded("controlify")) {
            controller = new ControlifyInput();
        } else if (CompatUtil.isModLoaded("controllable")) {
            controller = new ControllableInput();
        } else if (CompatUtil.isModLoaded("midnightcontrols")) {
            controller = new MidnightControlsInput();
        }
    }

    public static float axis(KeyMapping positive, KeyMapping negative) {
        return strength(positive) - strength(negative);
    }

    public static KeyMapping throttleKey() {
        KeyMapping attack = Minecraft.getInstance().options.keyAttack;
        // Explicit aircraft mappings take precedence over the default right trigger.
        return controller.strength(KeyBindings.throttleUp) < 0 && controller.strength(attack) >= 0 ? attack : KeyBindings.throttleUp;
    }

    public static float throttle() {
        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() != null || !client.isWindowActive()
            || KeyBindings.isPhysicalDown(KeyBindings.throttleUp) || KeyBindings.isPhysicalDown(KeyBindings.throttleDown)) {
            return -1;
        }
        float value = controller.strength(throttleKey());
        return Float.isFinite(value) && value >= 0 ? Mth.clamp(value, 0, 1) : -1;
    }

    public static boolean consumesAttack() {
        Minecraft client = Minecraft.getInstance();
        return client.player != null && client.player.getRootVehicle() instanceof AirplaneEntity airplane
               && airplane.getControllingPassenger() == client.player && throttleKey() == client.options.keyAttack
               && controller.strength(client.options.keyAttack) > 0 && !KeyBindings.isPhysicalDown(client.options.keyAttack);
    }

    public static float strength(KeyMapping key) {
        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() != null || !client.isWindowActive()) {
            return 0;
        }
        float value = controller.strength(key);
        KeyMapping fallback = KeyBindings.getFallbackKey(key);
        if (value < 0 && fallback != null) {
            value = controller.strength(fallback);
        }
        if (!Float.isFinite(value) || value < 0) {
            return key.isDown() ? 1 : 0;
        }
        return KeyBindings.isPhysicalDown(key) ? 1 : Mth.clamp(value, 0, 1);
    }
}
