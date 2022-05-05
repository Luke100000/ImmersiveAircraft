package immersive_airships.mixin;

import immersive_airships.entity.AirshipEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
    @Inject(method = "setupTransforms", at = @At("TAIL"))
    public <E extends Entity> void render(T entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta, CallbackInfo ci) {
        if (entity.getRootVehicle() != entity && entity.getRootVehicle() instanceof AirshipEntity airship) {
            matrices.translate(0, -entity.getHeightOffset(), 0);
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-airship.getPitch(tickDelta)));
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-airship.getRoll(tickDelta)));
            matrices.translate(0, entity.getHeightOffset(), 0);
        }
    }
}
