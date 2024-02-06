package immersive_aircraft.entity.weapons;

import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.util.Utils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Telescope extends Weapon {
    private final RotationalManager rotationalManager = new RotationalManager(this);

    int lastFireTick = 0;

    public Telescope(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        super(entity, stack, mount, slot);
    }

    @Override
    public void tick() {
        rotationalManager.tick();
        rotationalManager.pointTo(getEntity());

        lastFireTick--;
        if (lastFireTick == 0) {
            Entity pilot = getEntity().getControllingPassenger();
            if (pilot != null) {
                pilot.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0f, 1.0f);
            }
        }
    }

    @Override
    public void fire(Vector3f direction) {
        // pass
    }

    public Quaternionf getHeadTransform(float tickDelta) {
        Quaternionf quaternion = Utils.fromXYZ(0.0f, 0.0f, (float) (-getEntity().getRoll(tickDelta) / 180.0 * Math.PI));
        quaternion.mul(Utils.fromXYZ(0.0f, -rotationalManager.getYaw(tickDelta), 0.0f));
        quaternion.mul(Utils.fromXYZ(rotationalManager.getPitch(tickDelta), 0.0f, 0.0f));
        return quaternion;
    }

    @Override
    public void clientFire(int index) {
        Entity pilot = getEntity().getControllingPassenger();
        assert pilot != null;
        if (lastFireTick <= 0) {
            pilot.playSound(SoundEvents.SPYGLASS_USE, 1.0f, 1.0f);
        }
        lastFireTick = 2;
    }

    public Boolean isScoping() {
        return lastFireTick > 0;
    }
}
