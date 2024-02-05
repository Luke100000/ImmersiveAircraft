package immersive_aircraft.entity.misc;

import com.google.common.collect.ImmutableList;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class EntityGetterUtils {
    public static ImmutableList.Builder<VoxelShape> getVehicleCollisions(EntityGetter getter, Entity entity, AABB collisionBox) {
        List<Entity> vehicles = getter.getEntities(entity, collisionBox.inflate(16.0), VehicleEntity.class::isInstance);

        ImmutableList.Builder<VoxelShape> builder = ImmutableList.builder();
        for (Entity e : vehicles) {
            if (e instanceof VehicleEntity vehicle && e != entity) {
                for (AABB additionalShape : vehicle.getAdditionalShapes()) {
                    if (additionalShape.intersects(collisionBox.inflate(1.0E-7))) {
                        builder.add(Shapes.create(additionalShape));
                    }
                }
            }
        }

        return builder;
    }
}
