package immersive_aircraft.mixin.client;

import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private ClientLevel level;

    @Unique
    private boolean immersiveAircraft$wasPassenger;

    @Inject(method = "handleSetEntityPassengersPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;ejectPassengers()V"))
    private void immersiveAircraft$rememberPassenger(ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        Entity vehicle = level.getEntity(packet.getVehicle());
        Minecraft client = Minecraft.getInstance();
        immersiveAircraft$wasPassenger = client.player != null && vehicle != null && vehicle.hasIndirectPassenger(client.player);
    }

    @Inject(method = "handleSetEntityPassengersPacket(Lnet/minecraft/network/protocol/game/ClientboundSetPassengersPacket;)V", at = @At("TAIL"))
    public void onEntityPassengersSetInject(ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || immersiveAircraft$wasPassenger
                || !(level.getEntity(packet.getVehicle()) instanceof VehicleEntity vehicle)
                || !vehicle.hasIndirectPassenger(client.player)) {
            return;
        }

        client.player.yRotO = vehicle.getYRot();
        client.player.setYRot(vehicle.getYRot());
        client.player.setYHeadRot(vehicle.getYRot());

        Component message = Component.translatable("mount.onboard", KeyBindings.dismount.getTranslatedKeyMessage());
        if (vehicle.getPassengerSpace() > 1) {
            message = message.copy().append(" - ").append(Component.translatable("immersive_aircraft.seat_controls"));
        }
        client.gui.hud.setOverlayMessage(message, false);
    }
}
