package immersive_aircraft.mixin;

import com.google.common.collect.ImmutableList;
import immersive_aircraft.entity.misc.EntityGetterUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("CommentedOutCode")
@Mixin(EntityGetter.class)
public interface EntityGetterMixin {
    @Shadow
    List<Entity> getEntities(@Nullable Entity entity, AABB area, Predicate<Entity> predicate);

    /**
     * @author Luke100000
     * @reason Forge lacks Injects into interfaces, but I need to inject additional collisions
     */
    @Overwrite
    default List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB collisionBox) {
        if (collisionBox.getSize() < 1.0E-7) {
            return List.of();
        }
        Predicate<Entity> predicate = entity == null ? EntitySelector.CAN_BE_COLLIDED_WITH : EntitySelector.NO_SPECTATORS.and(entity::canCollideWith);
        List<Entity> list = this.getEntities(entity, collisionBox.inflate(1.0E-7), predicate);
        ImmutableList.Builder<VoxelShape> collisions = EntityGetterUtils.getVehicleCollisions((EntityGetter) this, entity, collisionBox);
        for (Entity entity2 : list) {
            collisions.add(Shapes.create(entity2.getBoundingBox()));
        }
        return collisions.build();
    }

    /*
    // Works with Fabrics Mixin, but not with Forge SpongeForge Mixin
    @Inject(method = "getEntityCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",
            at = @At("RETURN"),
            cancellable = true)
    default void ia$extendEntityCollisions(@Nullable Entity entity, AABB collisionBox, CallbackInfoReturnable<List<VoxelShape>> cir) {
        List<VoxelShape> vanillaCollisions = cir.getReturnValue();
        if (collisionBox.getSize() >= 1.0E-7) {
            ImmutableList.Builder<VoxelShape> vehicleCollisions = EntityGetterUtils.getVehicleCollisions((EntityGetter) (Object) this, entity, collisionBox);
            vehicleCollisions.addAll(vanillaCollisions);
            cir.setReturnValue(vehicleCollisions.build());
        }
    }
     */
}
