package immersive_aircraft.entity.weapons;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class Telescope extends Weapon {
    private final RotationalManager rotationalManager = new RotationalManager();

    int lastFireTick = 0;

    public Telescope(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        super(entity, stack, mount, slot);
    }

    @Override
    public void tick() {
        rotationalManager.tick();
        rotationalManager.pointTo(getEntity());

        lastFireTick--;
    }

    @Override
    public void fire(Vector3f direction) {
        // pass
    }

    public Quaternion getHeadTransform(float tickDelta) {
        Quaternion quaternion = Quaternion.fromXYZ(0.0f, 0.0f, (float) (-getEntity().getRoll(tickDelta) / 180.0 * Math.PI));
        quaternion.mul(Quaternion.fromXYZ(0.0f, -rotationalManager.getYaw(tickDelta), 0.0f));
        quaternion.mul(Quaternion.fromXYZ(rotationalManager.getPitch(tickDelta), 0.0f, 0.0f));
        return quaternion;
    }

    @Override
    public void clientFire(int index) {
        Entity pilot = getEntity().getControllingPassenger();
        assert pilot != null;
        pilot.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0f, 1.0f);
        lastFireTick = 2;
    }

    public Boolean isScoping() {
        return lastFireTick > 0;
    }
}
