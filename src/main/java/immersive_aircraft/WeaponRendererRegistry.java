package immersive_aircraft;

import immersive_aircraft.client.render.entity.weaponRenderer.SimpleWeaponRenderer;
import immersive_aircraft.client.render.entity.weaponRenderer.WeaponRenderer;
import immersive_aircraft.entity.weapon.Weapon;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class WeaponRendererRegistry {
    public static final Map<ResourceLocation, WeaponRenderer<? extends Weapon>> REGISTRY = new HashMap<>();

    public static void register(ResourceLocation id, WeaponRenderer<? extends Weapon> renderer) {
        REGISTRY.put(id, renderer);
    }

    static {
        register(Main.locate("rotary_cannon"), new SimpleWeaponRenderer(Main.locate("objects/rotary_cannon.bbmodel")));
        register(Main.locate("heavy_crossbow"), new SimpleWeaponRenderer(Main.locate("objects/heavy_crossbow.bbmodel")));
        register(Main.locate("telescope"), new SimpleWeaponRenderer(Main.locate("objects/telescope.bbmodel")));
        register(Main.locate("bomb_bay"), new SimpleWeaponRenderer(Main.locate("objects/bomb_bay.bbmodel")));
    }

    public static void bootstrap() {
        // nop
    }

    @SuppressWarnings("unchecked")
    public static <W extends Weapon> WeaponRenderer<W> get(W weapon) {
        return (WeaponRenderer<W>) REGISTRY.get(Item.REGISTRY.getNameForObject(weapon.getStack().getItem()));
    }
}
