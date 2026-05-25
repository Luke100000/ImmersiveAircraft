package immersive_aircraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Camera mainCamera;

    @Inject(method = "bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At("HEAD"), cancellable = false)
    public void immersiveAircraft$renderWorld(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
        Entity entity = mainCamera.entity();
        //noinspection ConstantValue
        if (entity != null && !mainCamera.isDetached() && entity.getRootVehicle() instanceof VehicleEntity vehicle) {
            // rotate camera
            if (vehicle.adaptPlayerRotation) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(vehicle.getRoll(partialTicks)));
                poseStack.mulPose(Axis.XP.rotationDegrees(vehicle.getViewXRot(partialTicks)));
            }

            // fetch eye offset
            float eye = entity.getEyeHeight();

            // transform eye offset to match aircraft rotation
            Vector3f offset = new Vector3f(0, -eye, 0);
            Quaternionf quaternion = Axis.XP.rotationDegrees(0.0f);
            quaternion.mul(Axis.YP.rotationDegrees(-vehicle.getViewYRot(partialTicks)));
            quaternion.mul(Axis.XP.rotationDegrees(vehicle.getViewXRot(partialTicks)));
            quaternion.mul(Axis.ZP.rotationDegrees(vehicle.getRoll(partialTicks)));
            offset.rotate(quaternion);

            // apply camera offset
            poseStack.mulPose(Axis.XP.rotationDegrees(mainCamera.xRot()));
            poseStack.mulPose(Axis.YP.rotationDegrees(mainCamera.yRot() + 180.0f));
            poseStack.translate(offset.x(), offset.y() + eye, offset.z());
            poseStack.mulPose(Axis.YP.rotationDegrees(-mainCamera.yRot() - 180.0f));
            poseStack.mulPose(Axis.XP.rotationDegrees(-mainCamera.xRot()));
        }
    }
}
