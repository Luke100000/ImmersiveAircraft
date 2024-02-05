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
        register(Main.locate("rotary_cannon"), new RotaryCannonRenderer());
        register(Main.locate("heavy_crossbow"), new HeavyCrossbowRenderer());
        register(Main.locate("telescope"), new TelescopeRenderer());
        register(Main.locate("bomb_bay"), new BombBayRenderer());
    }

    public static void bootstrap() {
        // nop
    }

    public static <W extends Weapon> WeaponRenderer<W> get(W weapon) {
        //noinspection unchecked
        return (WeaponRenderer<W>) REGISTRY.get(Registry.ITEM.getKey(weapon.getStack().getItem()));
    }
}
