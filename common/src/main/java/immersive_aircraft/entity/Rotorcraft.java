package immersive_aircraft.entity;

import immersive_aircraft.item.upgrade.AircraftStat;
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
                Mth.sin(-getYRot() * ((float) Math.PI / 180)),
                0.0f,
                Mth.cos(getYRot() * ((float) Math.PI / 180))
        ).normalize();
    }

    @Override
    public Vec3 getRightDirection() {
        return new Vec3(
                Mth.cos(-getYRot() * ((float) Math.PI / 180)),
                0.0,
                Mth.sin(getYRot() * ((float) Math.PI / 180))
        ).normalize();
    }

    @Override
    protected void convertPower(Vec3 direction) {
        Vec3 velocity = getDeltaMovement().multiply(1.0f, 0.0f, 1.0f);
        double drag = Math.abs(direction.dot(velocity.normalize()));
        Vec3 newVelocity = velocity.normalize()
                .lerp(direction, getProperties().get(AircraftStat.LIFT))
                .scale(velocity.length() * (drag * getProperties().get(AircraftStat.FRICTION) + (1.0 - getProperties().get(AircraftStat.FRICTION))));
        setDeltaMovement(
                newVelocity.x,
                getDeltaMovement().y,
                newVelocity.z
        );
    }
}
