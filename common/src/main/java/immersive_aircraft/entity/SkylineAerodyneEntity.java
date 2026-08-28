package immersive_aircraft.entity;

import immersive_aircraft.Items;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * A compact near-future rotorcraft with the responsive flight profile of the
 * quadrocopter. Its visual lift pods are animated by the dedicated renderer.
 */
public class SkylineAerodyneEntity extends QuadrocopterEntity {
    public SkylineAerodyneEntity(EntityType<? extends AircraftEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public Item asItem() {
        return Items.SKYLINE_AERODYNE.get();
    }
}
