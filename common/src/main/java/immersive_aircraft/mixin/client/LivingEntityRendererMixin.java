package immersive_aircraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.client.render.entity.renderer.utils.PassengerLivingEntityRenderState;
import immersive_aircraft.entity.VehicleEntity;
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
        if (entity.getRootVehicle() != entity && entity.getRootVehicle() instanceof VehicleEntity vehicle) {
            passengerState.immersive_aircraft$setVehicleXRot(vehicle.getViewXRot(tickDelta));
            passengerState.immersive_aircraft$setVehicleZRot(vehicle.getRoll(tickDelta));
        } else {
            passengerState.immersive_aircraft$setVehicleXRot(null);
            passengerState.immersive_aircraft$setVehicleZRot(null);
        }
    }

    @Inject(method = "setupRotations", at = @At("TAIL"))
    public void immersiveAircraft$setupRotations(S state, PoseStack matrices, float bodyYaw, float scale, CallbackInfo ci) {
        PassengerLivingEntityRenderState passengerState = (PassengerLivingEntityRenderState) state;
        Float vehicleXRot = passengerState.immersive_aircraft$getVehicleXRot();
        Float vehicleZRot = passengerState.immersive_aircraft$getVehicleZRot();
        if (vehicleXRot != null && vehicleZRot != null) {
            matrices.mulPose(Axis.XP.rotationDegrees(-vehicleXRot));
            matrices.mulPose(Axis.ZP.rotationDegrees(-vehicleZRot));
        }
    }
}
