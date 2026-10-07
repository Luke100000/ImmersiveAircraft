package immersive_aircraft.client.compat;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.bindings.ControlifyBindings;
import immersive_aircraft.client.ControllerInput;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public final class ControlifyInput implements ControllerInput {
    @Override
    public float strength(KeyMapping key) {
        ControlifyApi api = ControlifyApi.get();
        var controller = api.getCurrentController().orElse(null);
        if (controller == null || !api.currentInputMode().isController()) {
            return -1;
        }
        float value = -1;
        for (InputBindingSupplier supplier : ControlifyBindApi.get().getKeyCorrelation(key)) {
            InputBinding binding = supplier.onOrNull(controller);
            if (binding != null && !binding.isUnbound()) {
                value = Math.max(value, binding.analogueNow());
            }
        }
        if (value >= 0) return value;

        InputBindingSupplier supplier = vanillaBinding(key);
        if (supplier == null) return -1;
        InputBinding binding = supplier.onOrNull(controller);
        return binding != null && !binding.isUnbound() ? binding.analogueNow() : -1;
    }

    private static InputBindingSupplier vanillaBinding(KeyMapping key) {
        Options options = Minecraft.getInstance().options;
        if (key == options.keyLeft) return ControlifyBindings.WALK_LEFT;
        if (key == options.keyRight) return ControlifyBindings.WALK_RIGHT;
        if (key == options.keyUp) return ControlifyBindings.WALK_FORWARD;
        if (key == options.keyDown) return ControlifyBindings.WALK_BACKWARD;
        if (key == options.keyJump) return ControlifyBindings.JUMP;
        if (key == options.keyShift) return ControlifyBindings.SNEAK;
        return null;
    }
}
