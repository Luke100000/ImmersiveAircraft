package immersive_aircraft.mixin;

import immersive_aircraft.entity.InventoryVehicleEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "hasRidingInventory()Z", at = @At("HEAD"), cancellable = true)
    public void hasRidingInventory(CallbackInfoReturnable<Boolean> cir) {
        assert this.client.player != null;
        if (this.client.player.hasVehicle() && this.client.player.getVehicle() instanceof InventoryVehicleEntity) {
            cir.setReturnValue(true);
        }
    }
}
