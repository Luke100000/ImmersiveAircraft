package immersive_aircraft.entity.weapon;

import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

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
                // 1.20.1 plays the spyglass stop sound; 1.12.2 has none, use the bow hit sound
                pilot.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 1.0f);
            }
        }
    }

    @Override
    public void fire(Vec3d direction) {
        // pass
    }

    @Override
    public void clientFire(int index) {
        Entity pilot = getEntity().getControllingPassenger();
        if (pilot == null) {
            return;
        }
        if (lastFireTick <= 0) {
            pilot.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 1.0f);
        }
        lastFireTick = 2;
    }

    public boolean isScoping() {
        return lastFireTick > 0;
    }

    public RotationalManager getRotationalManager() {
        return rotationalManager;
    }
}
