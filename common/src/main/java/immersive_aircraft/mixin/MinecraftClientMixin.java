package immersive_aircraft.mixin;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.network.c2s.CommandMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    private int itemUseCooldown;

    @Inject(method = "doItemUse()V", at = @At("HEAD"), cancellable = true)
    private void injectDoItemUse(CallbackInfo ci) {
        if (player != null && player.getRootVehicle() instanceof AircraftEntity aircraft) {
            NetworkHandler.sendToServer(new CommandMessage(CommandMessage.Key.USE, aircraft.getVelocity()));
            this.itemUseCooldown = 4;
            ci.cancel();
        }
    }
}
