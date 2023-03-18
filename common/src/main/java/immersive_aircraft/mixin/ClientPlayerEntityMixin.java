package immersive_aircraft.mixin;

import com.mojang.authlib.GameProfile;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.c2s.CommandMessage;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
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
            NetworkHandler.sendToServer(new CommandMessage(CommandMessage.Key.INVENTORY, getVelocity()));
            ci.cancel();
        }
    }
}
