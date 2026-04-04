package immersive_aircraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {
    @Unique
    private static final ThreadLocal<float[]> ia$vehicleRotation = ThreadLocal.withInitial(() -> null);

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    public void ia$extractRenderState(T entity, S state, float partialTick, CallbackInfo ci) {
        if (entity.getRootVehicle() != entity && entity.getRootVehicle() instanceof VehicleEntity vehicle) {
            ia$vehicleRotation.set(new float[]{vehicle.getViewXRot(partialTick), vehicle.getRoll(partialTick)});
        } else {
            ia$vehicleRotation.set(null);
        }
    }

    @Inject(method = "setupRotations", at = @At("TAIL"))
    public void ia$setupRotations(S state, PoseStack poseStack, float bodyYaw, float partialTick, CallbackInfo ci) {
        float[] rotation = ia$vehicleRotation.get();
        if (rotation != null) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-rotation[0]));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-rotation[1]));
        }
    }
}
