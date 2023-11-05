package immersive_aircraft;

import immersive_aircraft.client.render.entity.weaponRenderer.RotaryCannonRenderer;
import immersive_aircraft.client.render.entity.weaponRenderer.WeaponRenderer;
import immersive_aircraft.entity.weapons.Weapon;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class WeaponRendererRegistry {
    public static final Map<ResourceLocation, WeaponRenderer<? extends Weapon>> REGISTRY = new HashMap<>();

    public static void register(ResourceLocation id, WeaponRenderer<? extends Weapon> renderer) {
        REGISTRY.put(id, renderer);
    }

    static {
        register(Main.locate("rotary_cannon"), new RotaryCannonRenderer());
    }

    public static void bootstrap() {
        // nop
    }

    public static <W extends Weapon> WeaponRenderer<W> get(W weapon) {
        //noinspection unchecked
        return (WeaponRenderer<W>) REGISTRY.get(Registry.ITEM.getKey(weapon.getStack().getItem()));
    }
}
