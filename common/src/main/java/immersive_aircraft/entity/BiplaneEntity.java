package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.entity.misc.Trail;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

public class BiplaneEntity extends AirplaneEntity {
    public BiplaneEntity(EntityType<? extends AircraftEntity> entityType, Level world) {
        super(entityType, world, true);
    }

    private final List<Trail> trails = List.of(new Trail(40), new Trail(40));

    public List<Trail> getTrails() {
        return trails;
    }

    protected void trail(Matrix4f transform, int index, float x, float y, float z) {
        Vector4f p0 = transformPosition(transform, x, y - 0.15f, z);
        Vector4f p1 = transformPosition(transform, x, y + 0.15f, z);

        float trailStrength = Math.max(0.0f, Math.min(1.0f, (float) (Math.sqrt(getDeltaMovement().length()) * (0.5f + (pressingInterpolatedX.getSmooth() * x) * 0.025f) - 0.25f)));
        getTrails().get(index).add(p0, p1, trailStrength);
    }

    @Override
    public Item asItem() {
        return Items.BIPLANE.get();
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            if (isWithinParticleRange()) {
                Matrix4f transform = getVehicleTransform();
                Matrix3f normalTransform = getVehicleNormalTransform();

                // Trails
                trail(transform, 0, -3.75f, 0.25f, 0.6f);
                trail(transform, 1, 3.75f, 0.25f, 0.6f);

                // Smoke
                float power = getEnginePower();
                if (power > 0.05) {
                    for (int i = 0; i < 1 + engineSpinUpStrength * 4; i++) {
                        Vector4f p = transformPosition(transform, 0.325f * (tickCount % 2 == 0 ? -1.0f : 1.0f), 0.5f, 0.8f);
                        Vector3f vel = transformVector(normalTransform, 0.2f * (tickCount % 2 == 0 ? -1.0f : 1.0f), 0.0f, 0.0f);
                        Vec3 velocity = getDeltaMovement();
                        if (random.nextFloat() < engineSpinUpStrength * 0.1) {
                            vel.mul(0.5f);
                            level().addParticle(ParticleTypes.SMALL_FLAME, p.x(), p.y(), p.z(), vel.x() + velocity.x, vel.y() + velocity.y, vel.z() + velocity.z);
                        } else {
                            level().addParticle(ParticleTypes.SMOKE, p.x, p.y, p.z, vel.x + velocity.x, vel.y + velocity.y, vel.z + velocity.z);
                        }
                    }
                }
            } else {
                trails.get(0).add(ZERO_VEC4, ZERO_VEC4, 0.0f);
                trails.get(1).add(ZERO_VEC4, ZERO_VEC4, 0.0f);
            }
        }
    }

    @Override
    public double getZoom() {
        return 3.0;
    }
}
