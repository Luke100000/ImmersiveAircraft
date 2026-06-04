package immersive_aircraft;

import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.entity.weapon.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class WeaponRegistry {
    public static final Map<Identifier, WeaponConstructor> REGISTRY = new HashMap<>();

    public static void register(Identifier id, WeaponConstructor constructor) {
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
        return REGISTRY.get(BuiltInRegistries.ITEM.getKey(weapon.getItem()));
    }

    public interface WeaponConstructor {
        Weapon create(VehicleEntity entity, ItemStack itemStack, WeaponMount mount, int slot);
    }
}
