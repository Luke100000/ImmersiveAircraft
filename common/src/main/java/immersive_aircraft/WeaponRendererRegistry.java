package immersive_aircraft;

import immersive_aircraft.client.render.entity.weaponRenderer.*;
import immersive_aircraft.entity.weapons.Weapon;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class WeaponRendererRegistry {
    public static final Map<ResourceLocation, WeaponRenderer<? extends Weapon>> REGISTRY = new HashMap<>();

    public static void register(ResourceLocation id, WeaponRenderer<? extends Weapon> renderer) {
        REGISTRY.put(id, renderer);
    }

    static {
        register(Main.locate("rotary_cannon"), new SimleWeaponRenderer("rotary_cannon"));
        register(Main.locate("heavy_crossbow"), new SimleWeaponRenderer("heavy_crossbow"));
        register(Main.locate("telescope"), new SimleWeaponRenderer("telescope"));
        register(Main.locate("bomb_bay"), new SimleWeaponRenderer("bomb_bay"));
    }

    public static void bootstrap() {
        // nop
    }

    public static <W extends Weapon> WeaponRenderer<W> get(W weapon) {
        //noinspection unchecked
        return (WeaponRenderer<W>) REGISTRY.get(Registry.ITEM.getKey(weapon.getStack().getItem()));
    }
}
