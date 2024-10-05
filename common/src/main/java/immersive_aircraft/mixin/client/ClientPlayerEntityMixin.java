package immersive_aircraft.mixin.client;

import com.mojang.authlib.GameProfile;
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

    @Inject(method = "isCrouching()Z", at = @At("HEAD"), cancellable = true)
    public void ia$isCrouching(CallbackInfoReturnable<Boolean> cir) {
        if (getRootVehicle() instanceof VehicleEntity) {
            cir.setReturnValue(false);
        }
    }
}
