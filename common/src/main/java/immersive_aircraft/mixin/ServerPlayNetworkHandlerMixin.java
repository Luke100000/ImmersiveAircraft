package immersive_aircraft.mixin;

import immersive_aircraft.entity.AircraftEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Inject(method = "method_29780(Lnet/minecraft/entity/Entity;)Z", at=@At("HEAD"), cancellable = true)
    private void method_29780(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.getRootVehicle() instanceof AircraftEntity) {
            cir.setReturnValue(false);
        }
    }
}
