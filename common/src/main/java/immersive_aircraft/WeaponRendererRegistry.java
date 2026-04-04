package immersive_aircraft;

import immersive_aircraft.client.render.entity.weaponRenderer.*;
import immersive_aircraft.entity.weapon.Weapon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class WeaponRendererRegistry {
    public static final Map<Identifier, WeaponRenderer<? extends Weapon>> REGISTRY = new HashMap<>();

    public static void register(Identifier id, WeaponRenderer<? extends Weapon> renderer) {
        REGISTRY.put(id, renderer);
    }

    static {
        register(Main.locate("rotary_cannon"), new SimpleWeaponRenderer("rotary_cannon"));
        register(Main.locate("heavy_crossbow"), new SimpleWeaponRenderer("heavy_crossbow"));
        register(Main.locate("telescope"), new SimpleWeaponRenderer("telescope"));
        register(Main.locate("bomb_bay"), new SimpleWeaponRenderer("bomb_bay"));
    }

    public static void bootstrap() {
        // nop
    }

    public static <W extends Weapon> WeaponRenderer<W> get(W weapon) {
        //noinspection unchecked
        return (WeaponRenderer<W>) REGISTRY.get(BuiltInRegistries.ITEM.getKey(weapon.getStack().getItem()));
    }
}
