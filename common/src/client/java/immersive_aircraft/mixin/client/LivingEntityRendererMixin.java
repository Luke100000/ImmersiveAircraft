package immersive_aircraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.client.state.VehicleRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void ia$extractVehicleRotations(LivingEntity entity, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
        VehicleRenderState vehicleState = (VehicleRenderState) state;
        if (entity.getRootVehicle() != entity && entity.getRootVehicle() instanceof VehicleEntity vehicle) {
            vehicleState.ia$setVehiclePitch(vehicle.getViewXRot(tickDelta));
            vehicleState.ia$setVehicleRoll(vehicle.getRoll(tickDelta));
        } else {
            vehicleState.ia$setVehiclePitch(0.0f);
            vehicleState.ia$setVehicleRoll(0.0f);
        }
    }

    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V", at = @At("TAIL"))
    private void ia$applyVehicleRotations(LivingEntityRenderState state, PoseStack poseStack, float bob, float yBodyRot, CallbackInfo ci) {
        VehicleRenderState vehicleState = (VehicleRenderState) state;
        float pitch = vehicleState.ia$getVehiclePitch();
        float roll = vehicleState.ia$getVehicleRoll();
        if (pitch != 0.0f || roll != 0.0f) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-roll));
        }
    }
}
