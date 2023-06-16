package immersive_aircraft.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

abstract public class Rotorcraft extends EngineAircraft {
    public Rotorcraft(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public Vector3f getDirection() {
        return new Vector3f(
                MathHelper.sin(-getYaw() * ((float)Math.PI / 180)),
                0.0f,
                MathHelper.cos(getYaw() * ((float)Math.PI / 180))
        ).normalize();
    }

    @Override
    protected void convertPower(Vec3d direction) {
        Vec3d velocity = getVelocity().multiply(1.0f, 0.0f, 1.0f);
        double drag = Math.abs(direction.dotProduct(velocity.normalize()));
        Vec3d newVelocity = velocity.normalize()
                .lerp(direction, getProperties().getLift())
                .multiply(velocity.length() * (drag * getProperties().getDriftDrag() + (1.0 - getProperties().getDriftDrag())));
        setVelocity(
                newVelocity.x,
                getVelocity().y,
                newVelocity.z
        );
    }
}
