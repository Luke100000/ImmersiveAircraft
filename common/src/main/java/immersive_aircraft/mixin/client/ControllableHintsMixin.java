package immersive_aircraft.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import immersive_aircraft.client.compat.ControllableInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

@Pseudo
@Mixin(targets = "com.mrcrayfish.controllable.client.overlay.ActionHintOverlay", remap = false)
public class ControllableHintsMixin {
    @Shadow @Final private Map<Integer, Action> actions;

    // Controllable 0.26.0 does not fire its GATHER_ACTIONS event.
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
    private void immersiveAircraft$addHints(CallbackInfo ci, @Local Map<ButtonBinding, Action> actions) {
        ControllableInput.addHints(actions);
    }

    @ModifyArg(method = "drawConsoleHints", at = @At(value = "INVOKE",
            target = "Ljava/util/stream/Stream;sorted(Ljava/util/Comparator;)Ljava/util/stream/Stream;"))
    private Comparator<Map.Entry<Integer, Action>> immersiveAircraft$orderConsoleHints(Comparator<Map.Entry<Integer, Action>> original) {
        return (first, second) -> ControllableInput.compareHints(first.getValue(), second.getValue());
    }

    @ModifyExpressionValue(method = "drawSidedHints", at = @At(value = "INVOKE", target = "Ljava/util/Map;keySet()Ljava/util/Set;"))
    private Set<Integer> immersiveAircraft$orderSidedHints(Set<Integer> original) {
        return ControllableInput.orderedButtons(actions);
    }
}
