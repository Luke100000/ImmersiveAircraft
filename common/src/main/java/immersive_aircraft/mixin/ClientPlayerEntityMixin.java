package immersive_aircraft.mixin;

import com.mojang.authlib.GameProfile;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.c2s.CommandMessage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayer {
    public ClientPlayerEntityMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "isInSneakingPose()Z", at = @At("HEAD"), cancellable = true)
    public void isInSneakingPoseInject(CallbackInfoReturnable<Boolean> cir) {
        if (getRootVehicle() instanceof AircraftEntity) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "openRidingInventory()V", at = @At("HEAD"), cancellable = true)
    public void isInSneakingPoseInject(CallbackInfo ci) {
        if (getRootVehicle() instanceof InventoryVehicleEntity) {
            NetworkHandler.sendToServer(new CommandMessage(CommandMessage.Key.INVENTORY, getDeltaMovement()));
            ci.cancel();
        }
    }
}
