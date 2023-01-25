package immersive_aircraft.mixin;

import immersive_aircraft.entity.AircraftEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RotationAxis;
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
    public void renderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        Entity entity = camera.getFocusedEntity();
        if (!camera.isThirdPerson() && entity != null && entity.getRootVehicle() instanceof AircraftEntity aircraft) {
            // rotate camera
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(aircraft.getRoll(tickDelta)));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(aircraft.getPitch(tickDelta)));

            // fetch eye offset
            float eye = entity.getStandingEyeHeight();

            // transform eye offset to match aircraft rotation
            Vector3f offset = new Vector3f(0, -eye, 0);
            Quaternionf quaternion = RotationAxis.POSITIVE_X.rotationDegrees(0.0f);
            quaternion.conjugateBy(RotationAxis.POSITIVE_Y.rotationDegrees(-aircraft.getYaw(tickDelta)));
            quaternion.conjugateBy(RotationAxis.POSITIVE_X.rotationDegrees(aircraft.getPitch(tickDelta)));
            quaternion.conjugateBy(RotationAxis.POSITIVE_Z.rotationDegrees(aircraft.getRoll(tickDelta)));
            offset.rotate(quaternion);

            // apply camera offset
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
            matrices.translate(offset.x, offset.y + eye, offset.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw() - 180.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-camera.getPitch()));
        }
    }
}
