package immersive_aircraft.mixin.client;

import immersive_aircraft.entity.PassengerPosture;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.PassengerPositionDescriptor;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public PlayerEntityRendererMixin(EntityRendererProvider.Context ctx, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "setModelProperties(Lnet/minecraft/client/player/AbstractClientPlayer;)V", at = @At("TAIL"))
    private void setModelPose(AbstractClientPlayer player, CallbackInfo ci) {
        if (player.getVehicle() instanceof VehicleEntity vehicle) {
            PassengerPositionDescriptor position = vehicle.getPassengerPosition(player);
            getModel().crouching = position != null && position.posture() == PassengerPosture.CROUCHING;
        } else {
            getModel().crouching = player.isCrouching();
        }
    }

    @Inject(method = "getRenderOffset(Lnet/minecraft/client/player/AbstractClientPlayer;F)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    private void getPassengerRenderOffset(AbstractClientPlayer player, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if (player.getVehicle() instanceof VehicleEntity vehicle) {
            PassengerPositionDescriptor position = vehicle.getPassengerPosition(player);
            cir.setReturnValue(position != null && position.posture() == PassengerPosture.CROUCHING
                    ? new Vec3(0.0, -0.125 * player.getScale(), 0.0)
                    : Vec3.ZERO);
        }
    }
}
