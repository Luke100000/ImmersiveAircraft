package immersive_aircraft.mixin.client;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHitboxDebugRenderer.class)
public class EntityHitboxDebugRendererMixin {
    @Inject(method = "showHitboxes", at = @At("TAIL"))
    private void ia$showAdditionalHitboxes(Entity entity, float partialTicks, boolean serverEntity, CallbackInfo ci) {
        if (entity instanceof VehicleEntity vehicle) {
            Vec3 renderOffset = entity.getPosition(partialTicks).subtract(entity.position());
            int color = serverEntity ? 0xFF00FF00 : -1;
            vehicle.getAdditionalShapes().forEach(shape -> Gizmos.cuboid(shape.move(renderOffset), GizmoStyle.stroke(color)));
        }
    }
}
