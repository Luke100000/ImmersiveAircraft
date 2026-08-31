package immersive_aircraft.mixin;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of the 1.16 {@code ServerPlayNetworkHandlerMixin}, which forced
 * {@code method_29780} (the "no blocks below" test) to false for aircraft so the anti-cheat
 * never treats a flying vehicle as floating.
 * <p>
 * 1.12.2 has no such helper - the test is inlined in {@code processVehicleMove} and its result
 * is consumed one tick later in {@link NetHandlerPlayServer#update()}. Clearing the flag there
 * is equivalent and covers both early-return paths of the packet handler. Without it a pilot on
 * a server without {@code allow-flight=true} is disconnected after 80 airborne ticks.
 */
@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin {
    @Shadow
    public EntityPlayerMP player;

    @Shadow
    private boolean vehicleFloating;

    @Shadow
    private int vehicleFloatingTickCount;

    @Inject(method = "update", at = @At("HEAD"))
    private void immersiveAircraft$allowVehicleFlight(CallbackInfo ci) {
        if (player != null && player.getLowestRidingEntity() instanceof VehicleEntity) {
            vehicleFloating = false;
            vehicleFloatingTickCount = 0;
        }
    }
}
