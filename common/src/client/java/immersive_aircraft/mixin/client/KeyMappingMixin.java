package immersive_aircraft.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import immersive_aircraft.client.MultiKeyMapping;
import net.minecraft.client.KeyMapping;
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
@Mixin(value = KeyMapping.class, priority = 100)
public class KeyMappingMixin {
    @Shadow
    @Final
    private static Map<String, KeyMapping> ALL;

    @Inject(method = "click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V", at = @At("HEAD"))
    private static void immersiveAircraft$onClick(InputConstants.Key key, CallbackInfo ci) {
        List<MultiKeyMapping> keyBinding = MultiKeyMapping.KEY_TO_BINDING.get(key);
        if (keyBinding != null) {
            keyBinding.forEach(v -> ((KeyMappingAccessorMixin) v).setClickCount(((KeyMappingAccessorMixin) v).getClickCount() + 1));
        }
    }

    @Inject(method = "set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V", at = @At("HEAD"))
    private static void immersiveAircraft$set(InputConstants.Key key, boolean pressed, CallbackInfo ci) {
        List<MultiKeyMapping> keyBinding = MultiKeyMapping.KEY_TO_BINDING.get(key);
        if (keyBinding != null) {
            keyBinding.forEach(v -> v.setDown(pressed));
        }
    }

    @Inject(method = "resetMapping()V", at = @At("HEAD"))
    private static void immersiveAircraft$resetMapping(CallbackInfo ci) {
        MultiKeyMapping.KEY_TO_BINDING.clear();
        for (KeyMapping keyBinding : ALL.values()) {
            if (keyBinding instanceof MultiKeyMapping kb) {
                MultiKeyMapping.KEY_TO_BINDING.computeIfAbsent(kb.customBoundKey, v -> new LinkedList<>()).add(kb);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "same(Lnet/minecraft/client/KeyMapping;)Z", at = @At("HEAD"), cancellable = true)
    private void immersiveAircraft$equals(KeyMapping other, CallbackInfoReturnable<Boolean> cir) {
        if (((Object) this) instanceof MultiKeyMapping || other instanceof MultiKeyMapping) {
            cir.setReturnValue(((Object) this) == other);
        }
    }
}
