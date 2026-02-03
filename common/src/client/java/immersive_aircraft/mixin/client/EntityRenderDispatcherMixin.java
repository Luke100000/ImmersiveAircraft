package immersive_aircraft.mixin.client;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHitboxDebugRenderer.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "showHitboxes", at = @At("TAIL"))
    private void ia$inject$showHitboxes(Entity entity, float tickDelta, boolean server, CallbackInfo ci) {
        if (entity instanceof VehicleEntity vehicle) {
            Vec3 position = entity.position();
            Vec3 interpolated = entity.getPosition(tickDelta);
            Vec3 delta = interpolated.subtract(position);
            int color = server ? 0xFF00FF00 : 0xFFFFFFFF;
            GizmoStyle style = GizmoStyle.stroke(color);
            for (AABB aABB : vehicle.getAdditionalShapes()) {
                Gizmos.cuboid(aABB.move(delta), style);
            }
        }
    }
}
