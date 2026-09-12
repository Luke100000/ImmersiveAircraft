package immersive_aircraft.mixin;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Redirect(method = "handleInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
    private AABB immersiveAircraft$getInteractionBoundingBox(Entity entity) {
        if (entity instanceof VehicleEntity vehicle) {
            return vehicle.getBoundingBoxForCulling();
        }
        return entity.getBoundingBox();
    }
}
