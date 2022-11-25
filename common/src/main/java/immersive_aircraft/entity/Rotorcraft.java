package immersive_aircraft.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

abstract public class Rotorcraft extends EngineAircraft {
    public Rotorcraft(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public Vec3d getDirection() {
        return new Vec3d(
                MathHelper.sin(-getYaw() * ((float)Math.PI / 180)),
                0.0,
                MathHelper.cos(getYaw() * ((float)Math.PI / 180))
        ).normalize();
    }

    @Override
    void updateController() {
        if (!hasPassengers()) {
            setEngineTarget(0.0f);
            return;
        } else {
            setEngineTarget(1.0f);
        }

        super.updateController();
    }
}
