package immersive_aircraft.mixin;

import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    @Final
    private Minecraft client;

    @Shadow
    private ClientLevel world;

    // Makes sure the dismount text reflects the actual keybinding
    @Inject(method = "onEntityPassengersSet(Lnet/minecraft/network/packet/s2c/play/EntityPassengersSetS2CPacket;)V", at = @At("TAIL"))
    public void onEntityPassengersSetInject(ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        Entity entity = this.world.getEntity(packet.getVehicle());
        if (entity == null) {
            return;
        }
        boolean bl = entity.hasIndirectPassenger(this.client.player);
        for (int i : packet.getPassengers()) {
            Entity entity2 = this.world.getEntity(i);
            if (entity2 != null && (entity2 == this.client.player || bl) && entity instanceof VehicleEntity) {
                assert this.client.player != null;
                this.client.player.yRotO = entity.getYRot();
                this.client.player.setYRot(entity.getYRot());
                this.client.player.setYHeadRot(entity.getYRot());
                this.client.gui.setOverlayMessage(Component.translatable("mount.onboard", KeyBindings.dismount.getTranslatedKeyMessage()), false);
            }
        }
    }
}
