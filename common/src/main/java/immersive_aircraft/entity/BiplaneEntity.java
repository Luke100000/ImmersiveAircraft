package immersive_aircraft.entity;

import immersive_aircraft.client.render.entity.renderer.Trail;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import net.minecraft.world.World;

import java.util.List;

public class BiplaneEntity extends AirplaneEntity {
    public BiplaneEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    List<List<Vec3d>> PASSENGER_POSITIONS = List.of(
            List.of(
                    new Vec3d(0.0f, -0.35f, -0.4f)
            )
    );

    private final List<Trail> trails = List.of(new Trail(40), new Trail(40));

    public List<Trail> getTrails() {
        return trails;
    }

    protected List<List<Vec3d>> getPassengerPositions() {
        return PASSENGER_POSITIONS;
    }

    private void trail(Matrix4f transform, int index, float x, float y, float z) {
        Vector4f p0 = new Vector4f(x, y - 0.1f, z, 1);
        p0.transform(transform);

        Vector4f p1 = new Vector4f(x, y + 0.1f, z, 1);
        p1.transform(transform);

        trails.get(index).add(p0, p1);
    }

    @Override
    public void tick() {
        super.tick();

        Matrix4f transform = Matrix4f.translate((float)getX(), (float)getY(), (float)getZ());
        transform.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-getYaw()));
        transform.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(getPitch()));
        transform.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(getRoll()));

        trail(transform, 0, -3.75f, 0.25f, 0.6f);
        trail(transform, 1, 3.75f, 0.25f, 0.6f);
    }
}
