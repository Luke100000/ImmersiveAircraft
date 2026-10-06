package immersive_aircraft.mixin.client;

import com.mojang.authlib.GameProfile;
import immersive_aircraft.client.MouseFlight;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayer {
    public ClientPlayerEntityMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "getViewYRot", at = @At("HEAD"), cancellable = true)
    private void immersiveAircraft$mouseFlightYaw(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (getRootVehicle() instanceof VehicleEntity vehicle && MouseFlight.isPiloting(vehicle)) {
            float yaw = getYRot();
            if (!MouseFlight.isEnabled(vehicle)) {
                yaw += vehicle.getViewYRot(tickDelta) - vehicle.getYRot();
            }
            cir.setReturnValue(yaw);
        }
    }

    @Inject(method = "isCrouching()Z", at = @At("HEAD"), cancellable = true)
    public void ia$isCrouching(CallbackInfoReturnable<Boolean> cir) {
        if (getRootVehicle() instanceof VehicleEntity) {
            cir.setReturnValue(false);
        }
    }
}
