package immersive_aircraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.VehicleCameraTransform;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Camera mainCamera;

    @Unique
    private DeltaTracker immersiveAircraft$deltaTracker;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void immersiveAircraft$captureDeltaTracker(DeltaTracker deltaTracker, CallbackInfo ci) {
        immersiveAircraft$deltaTracker = deltaTracker;
    }

    @ModifyArgs(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    public void immersiveAircraft$renderWorld(Args args) {
        PoseStack poseStack = args.get(1);
        Entity entity = mainCamera.entity();
        //noinspection ConstantValue
        if (entity != null && !mainCamera.isDetached() && entity.getRootVehicle() instanceof VehicleEntity vehicle) {
            float partialTicks = mainCamera.getCameraEntityPartialTicks(immersiveAircraft$deltaTracker);
            VehicleCameraTransform.apply(poseStack, partialTicks, entity, vehicle, mainCamera.xRot(), mainCamera.yRot());
        }
    }
}
