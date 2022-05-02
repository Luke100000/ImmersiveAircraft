package immersive_airships.mixin;

import immersive_airships.entity.AirshipEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;
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
        if (!camera.isThirdPerson() && camera.getFocusedEntity() != null && camera.getFocusedEntity().getRootVehicle() instanceof AirshipEntity airship) {
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(airship.getRoll(tickDelta)));
        }
    }
}
