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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This injection hack allows multiple assignments to keys
 */
@Mixin(KeyBinding.class)
public class KeyBindingMixin {
    @Shadow
    @Final
    private static Map<String, KeyBinding> KEYS_BY_ID;
    private static final Map<InputUtil.Key, List<KeyBinding>> immersiveAircraft$MULTI_KEY_TO_BINDINGS = new HashMap<>();


    @Inject(method = "<init>(Ljava/lang/String;Lnet/minecraft/client/util/InputUtil$Type;ILjava/lang/String;)V", at = @At("TAIL"))
    private void immersiveAircraft$init(String translationKey, InputUtil.Type type, int code, String category, CallbackInfo ci) {
        immersiveAircraft$addKeyBinding((KeyBinding)(Object)this);
    }

    @Inject(method = "onKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;)V", at = @At("HEAD"), cancellable = true)
    private static void immersiveAircraft$onKeyPressed(InputUtil.Key key, CallbackInfo ci) {
        List<KeyBinding> keyBinding = immersiveAircraft$MULTI_KEY_TO_BINDINGS.get(key);
        if (keyBinding != null) {
            keyBinding.forEach(v -> ((KeyBindingAccessorMixin)v).setTimesPressed(((KeyBindingAccessorMixin)v).getTimesPressed() + 1));
        }
        ci.cancel();
    }

    @Inject(method = "setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V", at = @At("HEAD"), cancellable = true)
    private static void immersiveAircraft$setKeyPressed(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
        List<KeyBinding> keyBinding = immersiveAircraft$MULTI_KEY_TO_BINDINGS.get(key);
        if (keyBinding != null) {
            keyBinding.forEach(v -> v.setPressed(pressed));
        }
        ci.cancel();
    }

    @Inject(method = "updateKeysByCode()V", at = @At("HEAD"))
    private static void immersiveAircraft$updateKeysByCode(CallbackInfo ci) {
        immersiveAircraft$MULTI_KEY_TO_BINDINGS.clear();
        for (KeyBinding keyBinding : KEYS_BY_ID.values()) {
            immersiveAircraft$addKeyBinding(keyBinding);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "equals(Lnet/minecraft/client/option/KeyBinding;)Z", at = @At("HEAD"), cancellable = true)
    private void immersiveAircraft$equals(KeyBinding other, CallbackInfoReturnable<Boolean> cir) {
        if (((Object)this) instanceof MultiKeyBinding || other instanceof MultiKeyBinding) {
            cir.setReturnValue(((Object)this) == other);
        }
    }

    private static void immersiveAircraft$addKeyBinding(KeyBinding keyBinding) {
        immersiveAircraft$MULTI_KEY_TO_BINDINGS.computeIfAbsent(((KeyBindingAccessorMixin)keyBinding).getBoundKey(), (k) -> new LinkedList<>()).add(keyBinding);
    }
}
