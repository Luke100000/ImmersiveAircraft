package immersive_aircraft;

import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.entity.weapon.BombBay;
import immersive_aircraft.entity.weapon.HeavyCrossbow;
import immersive_aircraft.entity.weapon.RotaryCannon;
import immersive_aircraft.entity.weapon.Telescope;
import immersive_aircraft.entity.weapon.Weapon;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class WeaponRegistry {
    public static final Map<ResourceLocation, WeaponConstructor> REGISTRY = new HashMap<>();

    public static void register(ResourceLocation id, WeaponConstructor constructor) {
        REGISTRY.put(id, constructor);
    }

    static {
        register(Main.locate("rotary_cannon"), RotaryCannon::new);
        register(Main.locate("heavy_crossbow"), HeavyCrossbow::new);
        register(Main.locate("telescope"), Telescope::new);
        register(Main.locate("bomb_bay"), BombBay::new);
    }

    public static void bootstrap() {
        // nop
    }

    public static WeaponConstructor get(ItemStack weapon) {
        return REGISTRY.get(Item.REGISTRY.getNameForObject(weapon.getItem()));
    }

    public interface WeaponConstructor {
        Weapon create(VehicleEntity entity, ItemStack itemStack, WeaponMount mount, int slot);
    }
}
