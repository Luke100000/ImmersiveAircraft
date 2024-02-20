package immersive_aircraft.client.render.entity.weaponRenderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.weapons.Weapon;
import net.minecraft.resources.ResourceLocation;

public class SimleWeaponRenderer extends WeaponRenderer<Weapon> {
    final ResourceLocation id;

    public SimleWeaponRenderer(String id) {
        this(Main.locate(id));
    }

    public SimleWeaponRenderer(ResourceLocation id) {
        this.id = id;
    }

    @Override
    protected ResourceLocation getModelId() {
        return id;
    }
}
