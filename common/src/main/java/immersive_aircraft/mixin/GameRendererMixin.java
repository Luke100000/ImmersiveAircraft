package immersive_aircraft.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.entity.AircraftEntity;
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
public class GameRendererMixin {
    @Shadow
    @Final
    private Camera camera;

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V"))
    public void renderWorld(float tickDelta, long limitTime, PoseStack matrices, CallbackInfo ci) {
        Entity entity = camera.getEntity();
        if (!camera.isDetached() && entity != null && entity.getRootVehicle() instanceof AircraftEntity aircraft) {
            // rotate camera
            matrices.mulPose(Axis.ZP.rotationDegrees(aircraft.getRoll(tickDelta)));
            matrices.mulPose(Axis.XP.rotationDegrees(aircraft.getViewXRot(tickDelta)));

            // fetch eye offset
            float eye = entity.getEyeHeight();

            // transform eye offset to match aircraft rotation
            Vector3f offset = new Vector3f(0, -eye, 0);
            Quaternionf quaternion = Axis.XP.rotationDegrees(0.0f);
            quaternion.conjugateBy(Axis.YP.rotationDegrees(-aircraft.getViewYRot(tickDelta)));
            quaternion.conjugateBy(Axis.XP.rotationDegrees(aircraft.getViewXRot(tickDelta)));
            quaternion.conjugateBy(Axis.ZP.rotationDegrees(aircraft.getRoll(tickDelta)));
            offset.rotate(quaternion);

            // apply camera offset
            matrices.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
            matrices.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0f));
            matrices.translate(offset.x, offset.y + eye, offset.z);
            matrices.mulPose(Axis.YP.rotationDegrees(-camera.getYRot() - 180.0f));
            matrices.mulPose(Axis.XP.rotationDegrees(-camera.getXRot()));
        }
    }
}
