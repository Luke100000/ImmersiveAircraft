package immersive_aircraft.mixin;

import immersive_aircraft.entity.AircraftEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
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
        Entity entity = camera.getFocusedEntity();
        if (!camera.isThirdPerson() && entity != null && entity.getRootVehicle() instanceof AircraftEntity aircraft) {
            matrices.translate(0.0f, -entity.getStandingEyeHeight(), 0.0f);
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(aircraft.getRoll(tickDelta)));
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(aircraft.getPitch(tickDelta)));
            matrices.translate(0.0f, entity.getStandingEyeHeight(), 0.0f);
        }
    }
}
