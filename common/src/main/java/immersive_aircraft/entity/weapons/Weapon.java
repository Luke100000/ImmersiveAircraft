package immersive_aircraft.entity.weapons;

import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import net.minecraft.world.item.ItemStack;

public abstract class Weapon {
    private final VehicleEntity entity;
    private final ItemStack stack;
    private final WeaponMount mount;
    private final int slot;

    public Weapon(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        this.entity = entity;
        this.stack = stack;
        this.mount = mount;
        this.slot = slot;
    }

    public VehicleEntity getEntity() {
        return entity;
    }

    public ItemStack getStack() {
        return stack;
    }

    public WeaponMount getMount() {
        return mount;
    }

    public int getSlot() {
        return slot;
    }

    public abstract void tick();

    public abstract void clientFire();
}
