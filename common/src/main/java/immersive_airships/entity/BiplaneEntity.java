package immersive_airships.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class BiplaneEntity extends AirplaneEntity {
    public BiplaneEntity(EntityType<? extends AirshipEntity> entityType, World world) {
        super(entityType, world);
    }
}
