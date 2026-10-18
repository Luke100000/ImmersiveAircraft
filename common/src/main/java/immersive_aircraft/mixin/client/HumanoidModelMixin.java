package immersive_aircraft.mixin.client;

import immersive_aircraft.client.render.entity.renderer.utils.PassengerLivingEntityRenderState;
import immersive_aircraft.entity.PassengerPosture;
import immersive_aircraft.entity.misc.PassengerPositionDescriptor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("HEAD"))
    private void setPassengerPosture(HumanoidRenderState state, CallbackInfo ci) {
        PassengerLivingEntityRenderState passengerState = (PassengerLivingEntityRenderState) state;
        if (passengerState.immersive_aircraft$getVehicleXRot() == null) return;

        PassengerPositionDescriptor position = passengerState.immersive_aircraft$getPassengerPosition();
        PassengerPosture posture = position == null ? PassengerPosture.SEATED : position.posture();
        state.isPassenger = posture == PassengerPosture.SEATED;
        state.isCrouching = posture == PassengerPosture.CROUCHING;
    }
}
