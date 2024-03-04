package immersive_aircraft.entity;

import com.mojang.math.Axis;
import immersive_aircraft.Items;
import immersive_aircraft.entity.misc.Trail;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class CargoAirshipEntity extends AirshipEntity {
    public CargoAirshipEntity(EntityType<? extends AircraftEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public Item asItem() {
        return Items.CARGO_AIRSHIP.get();
    }

    private final List<Trail> trails = List.of(
            new Trail(15, 0.5f),
            new Trail(11, 0.5f),
            new Trail(11, 0.5f)
    );

    public List<Trail> getTrails() {
        return trails;
    }

    @Override
    protected void addTrails(Matrix4f transform) {
        Matrix4f tr = new Matrix4f(transform);
        tr.translate(new Vector3f(0.0f, 0.4f, -1.2f));
        tr.rotate(Axis.ZP.rotationDegrees(engineRotation.getSmooth() * 50.0f));
        trail(tr, 0);

        tr = new Matrix4f(transform);
        tr.translate(new Vector3f(1.15625f, 2.5f, -1.2f));
        tr.rotate(Axis.ZP.rotationDegrees(engineRotation.getSmooth() * 65.0f));
        trail(tr, 1);

        tr = new Matrix4f(transform);
        tr.translate(new Vector3f(-1.15625f, 2.5f, -1.2f));
        tr.rotate(Axis.ZP.rotationDegrees(engineRotation.getSmooth() * 65.0f));
        trail(tr, 2);
    }
}
