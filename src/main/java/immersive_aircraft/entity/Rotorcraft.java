package immersive_aircraft.entity;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

abstract public class Rotorcraft extends EngineAircraft {
    public Rotorcraft(World world) {
        super(world);
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
    protected void convertPower(Vec3d direction) {
        Vec3d velocity = new Vec3d(getVelocity().x, 0.0f, getVelocity().z);
        double drag = Math.abs(direction.dotProduct(velocity.normalize()));
        Vec3d newVelocity = (velocity.normalize().scale((1.0 - getProperties().getLift())))
                .add(direction.scale(getProperties().getLift()))
                .scale(velocity.length() * (drag * getProperties().getDriftDrag() + (1.0 - getProperties().getDriftDrag())));
        setVelocity(
                newVelocity.x,
                getVelocity().y,
                newVelocity.z
        );
    }
}
