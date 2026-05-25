package immersive_aircraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.client.render.entity.renderer.utils.PassengerLivingEntityRenderState;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    public void mixin$extractRenderState(T entity, S entityRenderState, float f, CallbackInfo ci) {
        PassengerLivingEntityRenderState rs = (PassengerLivingEntityRenderState) entityRenderState;
        if (entity.getRootVehicle() != entity && entity.getRootVehicle() instanceof VehicleEntity vehicle) {
            rs.immersive_aircraft$setVehicleXRot(vehicle.getViewXRot(f));
            rs.immersive_aircraft$setVehicleZRot(vehicle.getRoll(f));
        } else {
            rs.immersive_aircraft$setVehicleXRot(null);
            rs.immersive_aircraft$setVehicleZRot(null);
        }
    }

    /**
     * Ensure the passenger's bodies rotate properly when their aircraft turns.
     */
    @Inject(method = "setupRotations", at = @At("TAIL"))
    public void render(S livingEntityRenderState, PoseStack poseStack, float f, float g, CallbackInfo ci) {
        PassengerLivingEntityRenderState rs = (PassengerLivingEntityRenderState) livingEntityRenderState;
        if (rs.immersive_aircraft$getVehicleXRot() != null && rs.immersive_aircraft$getVehicleZRot() != null) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-rs.immersive_aircraft$getVehicleXRot()));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-rs.immersive_aircraft$getVehicleZRot()));
        }
    }
}
