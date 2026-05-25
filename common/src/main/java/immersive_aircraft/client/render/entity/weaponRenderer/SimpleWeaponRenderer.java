package immersive_aircraft.client.render.entity.weaponRenderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.weapon.Weapon;
import net.minecraft.resources.Identifier;

public class SimpleWeaponRenderer extends WeaponRenderer<Weapon> {
    final Identifier id;

    public SimpleWeaponRenderer(String id) {
        this(Main.locate(id));
    }

    public SimpleWeaponRenderer(Identifier id) {
        this.id = id;
    }

    @Override
    protected Identifier getModelId() {
        return id;
    }
}
