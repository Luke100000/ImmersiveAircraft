package immersive_aircraft.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public abstract class Rotorcraft extends EngineAircraft {
    public Rotorcraft(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
    }

    @Override
    public Vector3f getForwardDirection() {
        return new Vector3f(
                Mth.sin(-getYRot() * ((float)Math.PI / 180)),
                0.0f,
                Mth.cos(getYRot() * ((float)Math.PI / 180))
        ).normalize();
    }

    @Override
    protected void convertPower(Vec3 direction) {
        Vec3 velocity = getDeltaMovement().multiply(1.0f, 0.0f, 1.0f);
        double drag = Math.abs(direction.dot(velocity.normalize()));
        Vec3 newVelocity = velocity.normalize()
                .lerp(direction, getProperties().getLift())
                .scale(velocity.length() * (drag * getProperties().getDriftDrag() + (1.0 - getProperties().getDriftDrag())));
        setDeltaMovement(
                newVelocity.x,
                getDeltaMovement().y,
                newVelocity.z
        );
    }
}
