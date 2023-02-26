package immersive_aircraft.mixin;

import immersive_aircraft.Entities;
import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
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
                this.client.player.prevYaw = entity.yaw;
                this.client.player.yaw = entity.yaw;
                this.client.player.setHeadYaw(entity.yaw);
                this.client.inGameHud.setOverlayMessage(new TranslatableText("mount.onboard", KeyBindings.dismount.getBoundKeyLocalizedText()), false);
            }
        }
    }

    @Inject(method = "onEntitySpawn(Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;)V", at = @At("TAIL"), cancellable = true)
    private void onEntitySpawnInject(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        if (Entities.hasType(packet.getEntityTypeId())) {
            Entity entity = packet.getEntityTypeId().create(world);

            if (entity != null) {
                int i = packet.getId();
                entity.updateTrackedPosition(packet.getX(), packet.getY(), packet.getZ());
                entity.refreshPositionAfterTeleport(packet.getX(), packet.getY(), packet.getZ());
                entity.pitch = (float)(packet.getPitch() * 360) / 256.0f;
                entity.yaw = (float)(packet.getYaw() * 360) / 256.0f;
                entity.setEntityId(i);
                entity.setUuid(packet.getUuid());
                this.world.addEntity(i, entity);

                ci.cancel();
            }
        }
    }
}
