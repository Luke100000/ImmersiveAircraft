package immersive_aircraft.client.compat;

import eu.midnightdust.midnightcontrols.ControlsMode;
import eu.midnightdust.midnightcontrols.client.MidnightControlsConfig;
import eu.midnightdust.midnightcontrols.client.compat.CompatHandler;
import eu.midnightdust.midnightcontrols.client.compat.MidnightControlsCompat;
import eu.midnightdust.midnightcontrols.client.controller.ButtonBinding;
import eu.midnightdust.midnightcontrols.client.controller.InputHandlers;
import eu.midnightdust.midnightcontrols.client.controller.InputManager;
import eu.midnightdust.midnightcontrols.client.enums.ButtonState;
import eu.midnightdust.midnightcontrols.client.enums.HudSide;
import eu.midnightdust.midnightcontrols.client.gui.MidnightControlsRenderer;
import immersive_aircraft.client.ControllerInput;
import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class MidnightControlsInput implements ControllerInput {
    public static void registerDefaults() {
        MidnightControlsCompat.registerCompatHandler(new CompatHandler() {
            @Override
            public void handle() {
                // Register after config loading, before automatic keybind import.
                MidnightControlsConfig.excludedKeybindings.add(KeyBindings.boost.getName());
                ButtonBinding boost = new ButtonBinding(KeyBindings.boost.getName(),
                        new int[]{GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN}, List.of(), InputHandlers::inGame, false);
                boost.setKeyBinding(KeyBindings.boost);
                ButtonBinding.GAMEPLAY_CATEGORY.registerBinding(boost);
                InputManager.registerBinding(boost);
            }
        });
    }

    @Override
    public float strength(KeyMapping key) {
        if (MidnightControlsConfig.controlsMode != ControlsMode.CONTROLLER
            || !MidnightControlsConfig.getController().isGamepad()) {
            return -1;
        }

        float value = -1;
        var bindings = InputManager.streamBindings().iterator();
        while (bindings.hasNext()) {
            ButtonBinding binding = bindings.next();
            if (binding.asKeyBinding().orElse(null) == key && !binding.isNotBound()) {
                value = Math.max(value, bindingValue(binding));
            }
        }
        return value;
    }

    private static float bindingValue(ButtonBinding binding) {
        if (!binding.isAvailable()) return 0;
        for (int button : binding.getButton()) {
            int axis = button % 500 - 100;
            if (axis == GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER || axis == GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER) {
                if (!binding.isPressed() && (binding.getButton().length > 1 || InputManager.getBindingState(binding).isPressed())) return 0;
                var controller = button >= 500 ? MidnightControlsConfig.getSecondController().orElse(null) : MidnightControlsConfig.getController();
                if (controller == null || !controller.isGamepad()) return 0;
                // MidnightControls 1.10 binarizes triggers and doesn't populate their BUTTON_VALUES.
                float value = (controller.getState().axes(axis) + 1) * 0.5f;
                return value <= MidnightControlsConfig.triggerDeadZone ? 0 : value;
            }
            if (ButtonBinding.isAxis(button)) break;
        }
        return InputManager.getBindingValue(binding, binding.isPressed() ? ButtonState.REPEAT : ButtonState.NONE);
    }

    private static ButtonBinding getBinding(KeyMapping key) {
        return InputManager.streamBindings()
                .filter(binding -> binding.asKeyBinding().orElse(null) == key && !binding.isNotBound())
                .findFirst().orElse(null);
    }

    public static boolean renderHints(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (MidnightControlsConfig.controlsMode != ControlsMode.CONTROLLER || client.screen != null
            || client.player == null || !(client.player.getRootVehicle() instanceof VehicleEntity)) return false;
        if (client.options.hideGui || !MidnightControlsConfig.hudEnable) return true;

        int[] rows = new int[2];
        for (KeyMapping key : AircraftGuide.HINT_ORDER) {
            String label = AircraftGuide.label(key, client.player);
            if (label == null) continue;
            ButtonBinding binding = getBinding(AircraftGuide.binding(key, client.player));
            KeyMapping fallback = KeyBindings.getFallbackKey(key);
            if (binding == null && fallback != null) {
                binding = getBinding(fallback);
            }
            if (binding == null || !binding.isAvailable()) continue;

            boolean primary = key != KeyBindings.use && key != KeyBindings.dismount;
            boolean left = primary ? MidnightControlsConfig.hudSide == HudSide.LEFT : MidnightControlsConfig.hudSide == HudSide.RIGHT;
            int side = left ? 0 : 1;
            int width = MidnightControlsRenderer.getBindingIconWidth(binding) + 2 + client.font.width(Component.translatable(label));
            int x = left ? 2 : graphics.guiWidth() - 2 - width;
            int y = graphics.guiHeight() - 2 - MidnightControlsRenderer.ICON_SIZE * (rows[side] + 1);
            MidnightControlsRenderer.drawButtonTip(graphics, x, y, binding.getButton(), label, true, client);
            rows[side]++;
        }
        return true;
    }
}
