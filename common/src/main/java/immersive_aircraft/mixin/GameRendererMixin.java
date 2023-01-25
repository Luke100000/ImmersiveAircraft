package immersive_aircraft.mixin;

import immersive_aircraft.entity.AircraftEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.RotationAxis;
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
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(aircraft.getRoll(tickDelta)));
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(aircraft.getPitch(tickDelta)));

            // fetch eye offset
            float eye = entity.getStandingEyeHeight();

            // transform eye offset to match aircraft rotation
            Vec3f offset = new Vec3f(0, -eye, 0);
            Quaternion quaternion = Vec3f.POSITIVE_X.getDegreesQuaternion(0.0f);
            quaternion.hamiltonProduct(Vec3f.POSITIVE_Y.getDegreesQuaternion(-aircraft.getYaw(tickDelta)));
            quaternion.hamiltonProduct(Vec3f.POSITIVE_X.getDegreesQuaternion(aircraft.getPitch(tickDelta)));
            quaternion.hamiltonProduct(RotationAxis.POSITIVE_Z.rotationDegrees(aircraft.getRoll(tickDelta)));
            offset.rotate(quaternion);

            // apply camera offset
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0f));
            matrices.translate(offset.getX(), offset.getY() + eye, offset.getZ());
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw() - 180.0f));
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-camera.getPitch()));
        }
    }
}
