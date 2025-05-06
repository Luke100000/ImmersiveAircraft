package immersive_aircraft;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ItemColors {
    public static Map<Supplier<Item>, ItemColor> ITEM_COLOR_PROVIDERS = new HashMap<>() {{
        put(Items.WARSHIP, getDyeColor(0xFFECC88C));
        put(Items.AIRSHIP, getDyeColor(0xFFECC88C));
        put(Items.CARGO_AIRSHIP, getDyeColor(0xFFECC88C));
    }};

    public static ItemColor getDyeColor(int defaultColor) {
        return (item, layer) -> {
            if (layer != 0) {
                return -1;
            } else {
                return DyedItemColor.getOrDefault(item, defaultColor);
            }
        };
    }
}
