package immersive_aircraft.mixin;

import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private ClientWorld world;

    // Makes sure the dismount text reflects the actual keybinding
    @Inject(method = "onEntityPassengersSet(Lnet/minecraft/network/packet/s2c/play/EntityPassengersSetS2CPacket;)V", at = @At("TAIL"))
    public void onEntityPassengersSetInject(EntityPassengersSetS2CPacket packet, CallbackInfo ci) {
        Entity entity = this.world.getEntityById(packet.getId());
        if (entity == null) {
            return;
        }
        boolean bl = entity.hasPassengerDeep(this.client.player);
        for (int i : packet.getPassengerIds()) {
            Entity entity2 = this.world.getEntityById(i);
            if (entity2 != null && (entity2 == this.client.player || bl) && entity instanceof VehicleEntity) {
                assert this.client.player != null;
                this.client.player.prevYaw = entity.getYaw();
                this.client.player.setYaw(entity.getYaw());
                this.client.player.setHeadYaw(entity.getYaw());
                this.client.inGameHud.setOverlayMessage(new TranslatableText("mount.onboard", KeyBindings.dismount.getBoundKeyLocalizedText()), false);
            }
        }
    }
}
