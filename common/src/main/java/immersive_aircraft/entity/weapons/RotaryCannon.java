package immersive_aircraft.entity.weapons;

import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class RotaryCannon extends Weapon {
    float yaw = 0.0f;
    float pitch = 0.0f;
    float rotating = 0.0f;

    public RotaryCannon(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        super(entity, stack, mount, slot);
    }

    @Override
    public void tick() {
        // todo use the "right" passenger
        Entity pilot = getEntity().getControllingPassenger();
        if (pilot != null) {
            yaw = (float) (pilot.getYHeadRot() / 180 * Math.PI);
            pitch = (float) (pilot.getXRot() / 180 * Math.PI);
        }
    }

    @Override
    public void clientFire() {
        float old = rotating;
        rotating += 0.1;

        if (Math.floor(old) != Math.floor(rotating)) {
            // shoot
        }
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRotating() {
        return rotating;
    }
}
