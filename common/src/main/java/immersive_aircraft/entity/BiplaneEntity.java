package immersive_aircraft.entity;

import immersive_aircraft.entity.misc.Trail;
import net.minecraft.entity.EntityType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.List;

public class BiplaneEntity extends AirplaneEntity {
    public BiplaneEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    List<List<Vec3d>> PASSENGER_POSITIONS = List.of(List.of(new Vec3d(0.0f, -0.35f, -0.4f)));

    protected List<List<Vec3d>> getPassengerPositions() {
        return PASSENGER_POSITIONS;
    }

    private final List<Trail> trails = List.of(new Trail(40), new Trail(40));

    public List<Trail> getTrails() {
        return trails;
    }

    private void trail(Matrix4f transform, int index, float x, float y, float z) {
        Vector4f p0 = transformPosition(transform, x, y - 0.15f, z);
        Vector4f p1 = transformPosition(transform, x, y + 0.15f, z);

        float trailStrength = Math.max(0.0f, Math.min(1.0f, (float)(getVelocity().length() * (0.75f + (pressingInterpolatedX.getSmooth() * x) * 0.025f) - 0.25f)));
        trails.get(index).add(p0, p1, trailStrength);
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient) {
            if (isWithinParticleRange()) {
                Matrix4f transform = getVehicleTransform();
                Matrix3f normalTransform = getVehicleNormalTransform();

                // Trails
                trail(transform, 0, -3.75f, 0.25f, 0.6f);
                trail(transform, 1, 3.75f, 0.25f, 0.6f);

                // Smoke
                float power = getEnginePower();
                if (power > 0.0) {
                    Vector4f p = transformPosition(transform, 0.325f * (age % 4 == 0 ? -1.0f : 1.0f), 0.5f, 0.8f);
                    Vec3f vel = transformVector(normalTransform, 0.2f * (age % 4 == 0 ? -1.0f : 1.0f), 0.0f, 0.0f);
                    Vec3d velocity = getVelocity();
                    world.addParticle(ParticleTypes.SMOKE, p.getX(), p.getY(), p.getZ(), vel.getX() + velocity.x, vel.getY() + velocity.y, vel.getZ() + velocity.z);

                    // Engine sounds
                    if (age % 4 == 0) {
                        world.playSound(getX(), getY(), getZ(), SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, getSoundCategory(), 0.25f, 3f, false);
                    }
                }
            } else {
                trails.get(0).add(ZERO_VEC4, ZERO_VEC4, 0.0f);
                trails.get(1).add(ZERO_VEC4, ZERO_VEC4, 0.0f);
            }
        }
    }
}
