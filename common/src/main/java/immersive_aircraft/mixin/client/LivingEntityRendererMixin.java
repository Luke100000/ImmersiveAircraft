package immersive_aircraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.client.render.entity.renderer.utils.PassengerLivingEntityRenderState;
import immersive_aircraft.entity.PassengerPosture;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.PassengerPositionDescriptor;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    public void immersiveAircraft$extractRenderState(T entity, S state, float tickDelta, CallbackInfo ci) {
        PassengerLivingEntityRenderState passengerState = (PassengerLivingEntityRenderState) state;
        if (entity.getVehicle() instanceof VehicleEntity vehicle) {
            passengerState.immersive_aircraft$setVehicleXRot(vehicle.getViewXRot(tickDelta));
            passengerState.immersive_aircraft$setVehicleYRot(vehicle.getViewYRot(tickDelta));
            passengerState.immersive_aircraft$setVehicleZRot(vehicle.getRoll(tickDelta));
            passengerState.immersive_aircraft$setPassengerPosition(vehicle.getPassengerPosition(entity));
        } else {
            passengerState.immersive_aircraft$setVehicleXRot(null);
            passengerState.immersive_aircraft$setVehicleYRot(0.0f);
            passengerState.immersive_aircraft$setVehicleZRot(null);
            passengerState.immersive_aircraft$setPassengerPosition(null);
        }
    }

    @Inject(method = "setupRotations", at = @At("TAIL"))
    public void immersiveAircraft$setupRotations(S state, PoseStack poseStack, float bodyYaw, float scale, CallbackInfo ci) {
        PassengerLivingEntityRenderState passengerState = (PassengerLivingEntityRenderState) state;
        Float vehicleXRot = passengerState.immersive_aircraft$getVehicleXRot();
        Float vehicleZRot = passengerState.immersive_aircraft$getVehicleZRot();
        if (vehicleXRot == null || vehicleZRot == null) return;

        // Tilt in vehicle axes even when the passenger faces sideways.
        float relativeYaw = bodyYaw - passengerState.immersive_aircraft$getVehicleYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(relativeYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-vehicleXRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-vehicleZRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(-relativeYaw));

        PassengerPositionDescriptor position = passengerState.immersive_aircraft$getPassengerPosition();
        if (position != null) {
            float pivot = position.posture() == PassengerPosture.SEATED ? 0.6f : 0.0f;
            poseStack.translate(0.0f, pivot, 0.0f);
            poseStack.mulPose(Axis.XP.rotation(-position.pitch()));
            poseStack.mulPose(Axis.ZP.rotation(-position.roll()));
            poseStack.translate(0.0f, -pivot, 0.0f);
        }
    }
}
