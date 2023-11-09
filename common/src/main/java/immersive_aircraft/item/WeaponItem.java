package immersive_aircraft.item;

import immersive_aircraft.entity.misc.WeaponMount;
import net.minecraft.world.item.Item;

public class WeaponItem extends Item {
    private final WeaponMount.Type mountType;

    public WeaponItem(Properties settings, WeaponMount.Type mountType) {
        super(settings);

        this.mountType = mountType;
    }

    public WeaponMount.Type getMountType() {
        return mountType;
    }
}
