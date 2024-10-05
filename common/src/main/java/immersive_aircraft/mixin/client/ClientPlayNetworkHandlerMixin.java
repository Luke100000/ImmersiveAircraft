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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private ClientLevel level;

    // Makes sure the dismount text reflects the actual keybinding
    @Inject(method = "handleSetEntityPassengersPacket(Lnet/minecraft/network/protocol/game/ClientboundSetPassengersPacket;)V", at = @At("TAIL"))
    public void onEntityPassengersSetInject(ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        Entity entity = this.level.getEntity(packet.getVehicle());
        if (entity == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        assert minecraft.player != null;
        boolean bl = entity.hasIndirectPassenger(minecraft.player);
        for (int i : packet.getPassengers()) {
            Entity entity2 = this.level.getEntity(i);
            if (entity2 != null && (entity2 == minecraft.player || bl) && entity instanceof VehicleEntity) {
                assert minecraft.player != null;
                minecraft.player.yRotO = entity.getYRot();
                minecraft.player.setYRot(entity.getYRot());
                minecraft.player.setYHeadRot(entity.getYRot());
                minecraft.gui.setOverlayMessage(Component.translatable("mount.onboard", KeyBindings.dismount.getTranslatedKeyMessage()), false);
            }
        }
    }
}
