package immersive_aircraft.mixin;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {
    @Inject(method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;", at = @At("RETURN"), cancellable = true)
    private static void ia$getEntityHitResult(Entity shooter, Vec3 startVec, Vec3 endVec, AABB boundingBox, Predicate<Entity> filter, double distance, CallbackInfoReturnable<EntityHitResult> cir) {
        ia$vehicleTrace(cir.getReturnValue(), shooter.level(), shooter, startVec, endVec, boundingBox,filter, 0.0f, distance).ifPresent(cir::setReturnValue);
    }

    @Inject(method = "getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;", at = @At("RETURN"), cancellable = true)
    private static void ia$getEntityHitResult(Level level, Entity projectile, Vec3 startVec, Vec3 endVec, AABB boundingBox, Predicate<Entity> filter, float inflationAmount, CallbackInfoReturnable<EntityHitResult> cir) {
        ia$vehicleTrace(cir.getReturnValue(), level, projectile, startVec, endVec, boundingBox, filter, inflationAmount, Double.MAX_VALUE).ifPresent(cir::setReturnValue);
    }

    @Unique
    private static Optional<EntityHitResult> ia$vehicleTrace(@Nullable EntityHitResult previous, Level level, @Nullable Entity source, Vec3 startVec, Vec3 endVec, AABB boundingBox, Predicate<Entity> filter, float inflationAmount, double distance) {
        double bestDistance = previous == null ? distance : previous.getLocation().distanceToSqr(startVec);
        Entity entity = null;
        Vec3 collision = null;

        for (Entity e : level.getEntities(source, boundingBox.inflate(16.0), VehicleEntity.class::isInstance)) {
            if (e instanceof VehicleEntity vehicle && filter.test(vehicle)) {
                for (AABB aabb : vehicle.getAdditionalShapes()) {
                    Optional<Vec3> optionalCollision = aabb.inflate(inflationAmount).clip(startVec, endVec);
                    if (optionalCollision.isPresent()) {
                        Vec3 newCollision = optionalCollision.get();
                        double dist = startVec.distanceToSqr(newCollision);
                        if (dist < bestDistance) {
                            entity = vehicle;
                            collision = newCollision;
                            bestDistance = dist;
                        }
                    }
                }
            }
        }

        return entity == null ? Optional.empty() : Optional.of(new EntityHitResult(entity, collision));
    }
}
