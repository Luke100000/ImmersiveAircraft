package immersive_aircraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.entity.PassengerPosture;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.PassengerPositionDescriptor;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
    @Inject(method = "setupRotations", at = @At("TAIL"))
    public void render(T entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale, CallbackInfo ci) {
        if (entity.getVehicle() instanceof VehicleEntity vehicle) {
            // Tilt in vehicle axes even when the passenger faces sideways.
            float relativeYaw = yBodyRot - vehicle.getViewYRot(partialTick);
            poseStack.mulPose(Axis.YP.rotationDegrees(relativeYaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(-vehicle.getViewXRot(partialTick)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-vehicle.getRoll(partialTick)));
            poseStack.mulPose(Axis.YP.rotationDegrees(-relativeYaw));

            PassengerPositionDescriptor position = vehicle.getPassengerPosition(entity);
            if (position != null) {
                float pivot = position.posture() == PassengerPosture.SEATED ? 0.6f : 0.0f;
                poseStack.translate(0.0f, pivot, 0.0f);
                poseStack.mulPose(Axis.XP.rotation(-position.pitch()));
                poseStack.mulPose(Axis.ZP.rotation(-position.roll()));
                poseStack.translate(0.0f, -pivot, 0.0f);
            }
        }
    }
}
