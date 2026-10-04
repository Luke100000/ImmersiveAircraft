package immersive_aircraft.mixin.client;

import immersive_aircraft.client.AircraftInput;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class AircraftControlsMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void consumeThrottlePress(CallbackInfoReturnable<Boolean> cir) {
        if (AircraftInput.consumesAttack()) {
            cir.setReturnValue(false);
        }
    }

    @ModifyVariable(method = "continueAttack", at = @At("HEAD"), argsOnly = true)
    private boolean consumeThrottleHold(boolean attacking) {
        return attacking && !AircraftInput.consumesAttack();
    }
}
