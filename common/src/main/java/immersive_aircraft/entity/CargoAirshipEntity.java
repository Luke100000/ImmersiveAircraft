package immersive_aircraft.entity;

import immersive_aircraft.Items;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class CargoAirshipEntity extends AirshipEntity {
    public CargoAirshipEntity(EntityType<? extends AircraftEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public Item asItem() {
        return Items.CARGO_AIRSHIP.get();
    }
}
