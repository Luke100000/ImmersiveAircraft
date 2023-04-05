package immersive_aircraft.mixin;

import immersive_aircraft.client.MultiKeyBinding;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This injection hack allows multiple assignments to my keys
 */
@Mixin(value = KeyBinding.class, priority = 100)
public class KeyBindingMixin {
    @Shadow
    @Final
    private static Map<String, KeyBinding> keysById;

    @Inject(method = "onKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;)V", at = @At("HEAD"))
    private static void immersiveAircraft$onKeyPressed(InputUtil.Key key, CallbackInfo ci) {
        List<MultiKeyBinding> keyBinding = MultiKeyBinding.KEY_TO_BINDING.get(key);
        if (keyBinding != null) {
            keyBinding.forEach(v -> ((KeyBindingAccessorMixin)v).setTimesPressed(((KeyBindingAccessorMixin)v).getTimesPressed() + 1));
        }
    }

    @Inject(method = "setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V", at = @At("HEAD"))
    private static void immersiveAircraft$setKeyPressed(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
        List<MultiKeyBinding> keyBinding = MultiKeyBinding.KEY_TO_BINDING.get(key);
        if (keyBinding != null) {
            keyBinding.forEach(v -> v.setPressed(pressed));
        }
    }

    @Inject(method = "updateKeysByCode()V", at = @At("HEAD"))
    private static void immersiveAircraft$updateKeysByCode(CallbackInfo ci) {
        MultiKeyBinding.KEY_TO_BINDING.clear();
        for (KeyBinding keyBinding : keysById.values()) {
            if (keyBinding instanceof MultiKeyBinding) {
                MultiKeyBinding kb = (MultiKeyBinding)keyBinding;
                MultiKeyBinding.KEY_TO_BINDING.computeIfAbsent(kb.customBoundKey, v -> new LinkedList<>()).add(kb);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "equals(Lnet/minecraft/client/option/KeyBinding;)Z", at = @At("HEAD"), cancellable = true)
    private void immersiveAircraft$equals(KeyBinding other, CallbackInfoReturnable<Boolean> cir) {
        if (((Object)this) instanceof MultiKeyBinding || other instanceof MultiKeyBinding) {
            cir.setReturnValue(((Object)this) == other);
        }
    }
}
