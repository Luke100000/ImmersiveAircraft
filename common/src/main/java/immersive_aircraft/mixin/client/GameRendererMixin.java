package immersive_aircraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import immersive_aircraft.entity.AircraftEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
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

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V"))
    public void renderWorld(float tickDelta, long limitTime, PoseStack matrices, CallbackInfo ci) {
        Entity entity = mainCamera.getEntity();
        if (!mainCamera.isDetached() && entity != null && entity.getRootVehicle() instanceof AircraftEntity vehicle) {
            // rotate camera
            if (vehicle.adaptPlayerRotation) {
                matrices.mulPose(Vector3f.ZP.rotationDegrees(vehicle.getRoll(tickDelta)));
                matrices.mulPose(Vector3f.XP.rotationDegrees(vehicle.getViewXRot(tickDelta)));
            }

            // fetch eye offset
            float eye = entity.getEyeHeight();

            // transform eye offset to match aircraft rotation
            Vector3f offset = new Vector3f(0, -eye, 0);
            Quaternion quaternion = Vector3f.XP.rotationDegrees(0.0f);
            quaternion.mul(Vector3f.YP.rotationDegrees(-vehicle.getViewYRot(tickDelta)));
            quaternion.mul(Vector3f.XP.rotationDegrees(vehicle.getViewXRot(tickDelta)));
            quaternion.mul(Vector3f.ZP.rotationDegrees(vehicle.getRoll(tickDelta)));
            offset.transform(quaternion);

            // apply camera offset
            matrices.mulPose(Vector3f.XP.rotationDegrees(mainCamera.getXRot()));
            matrices.mulPose(Vector3f.YP.rotationDegrees(mainCamera.getYRot() + 180.0f));
            matrices.translate(offset.x(), offset.y() + eye, offset.z());
            matrices.mulPose(Vector3f.YP.rotationDegrees(-mainCamera.getYRot() - 180.0f));
            matrices.mulPose(Vector3f.XP.rotationDegrees(-mainCamera.getXRot()));
        }
    }
}
