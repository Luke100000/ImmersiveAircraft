package immersive_aircraft.mixin;

import com.google.common.collect.ImmutableList;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(value = EntityGetter.class, priority = 1500)
public interface EntityGetterMixin {
    @Shadow
    List<Entity> getEntities(@Nullable Entity entity, AABB area, Predicate<Entity> predicate);

    @Inject(method = "getEntityCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",
            at = @At(value = "HEAD"),
            cancellable = true)
    default void ia$extendEntityCollisions(Entity entity, AABB collisionBox, CallbackInfoReturnable<List<VoxelShape>> cir) {
        if (collisionBox.getSize() < 1.0E-7) {
            cir.setReturnValue(List.of());
        }
        Predicate<Entity> predicate = entity == null ? EntitySelector.CAN_BE_COLLIDED_WITH : EntitySelector.NO_SPECTATORS.and(entity::canCollideWith);
        List<Entity> list = this.getEntities(entity, collisionBox.inflate(1.0E-7), predicate);
        List<Entity> vehicles = this.getEntities(entity, collisionBox.inflate(16.0), VehicleEntity.class::isInstance);
        if (list.isEmpty() && vehicles.isEmpty()) {
            cir.setReturnValue(List.of());
        }
        ImmutableList.Builder<VoxelShape> builder = ImmutableList.builder();
        for (Entity entity2 : list) {
            builder.add(Shapes.create(entity2.getBoundingBox()));
        }
        for (Entity e : vehicles) {
            if (e instanceof VehicleEntity vehicle && e != entity) {
                for (AABB additionalShape : vehicle.getAdditionalShapes()) {
                    if (additionalShape.intersects(collisionBox.inflate(1.0E-7))) {
                        builder.add(Shapes.create(additionalShape));
                    }
                }
            }
        }
        cir.setReturnValue(builder.build());
    }
}
