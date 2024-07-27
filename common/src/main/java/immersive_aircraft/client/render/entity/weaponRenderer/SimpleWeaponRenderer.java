package immersive_aircraft.client.render.entity.weaponRenderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.weapon.Weapon;
import net.minecraft.resources.ResourceLocation;

public class SimpleWeaponRenderer extends WeaponRenderer<Weapon> {
    final ResourceLocation id;

    public SimpleWeaponRenderer(String id) {
        this(Main.locate(id));
    }

    public SimpleWeaponRenderer(ResourceLocation id) {
        this.id = id;
    }

    @Override
    protected ResourceLocation getModelId() {
        return id;
    }
}
