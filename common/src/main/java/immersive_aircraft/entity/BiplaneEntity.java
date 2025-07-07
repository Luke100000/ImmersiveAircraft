package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.entity.misc.TrailDescriptor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

public class BiplaneEntity extends AirplaneEntity {
    public BiplaneEntity(EntityType<? extends AircraftEntity> entityType, Level world) {
        super(entityType, world, true);
    }

    @Override
    public float getBaseTrailWidth(Matrix4f transform, int index, TrailDescriptor trail) {
        return (float) (Math.sqrt(getDeltaMovement().length()) * (0.5f - (pressingInterpolatedX.getSmooth() * trail.x()) * 0.025f) - 0.25f);
    }

    @Override
    public Item asItem() {
        return Items.BIPLANE.get();
    }

    @Override
    public void tick() {
        super.tick();

        // Smoke
        emitSmokeParticle(
                0.325f * (tickCount % 2 == 0 ? -1.0f : 1.0f), 0.5f, 0.8f,
                0.2f * (tickCount % 2 == 0 ? -1.0f : 1.0f), 0.0f, 0.0f
        );
    }

    @Override
    public double getZoom() {
        return 3.0;
    }
}
