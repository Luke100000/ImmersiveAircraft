package immersive_aircraft;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ItemColors {
    // Deprecated, use ITEM_COLOR_PROVIDERS, will get removed in 1.3.0
    public static Map<Item, ItemColor> ITEM_COLORS = new HashMap<>();

    public static Map<Supplier<Item>, ItemColor> ITEM_COLOR_PROVIDERS = new HashMap<>() {{
        put(Items.WARSHIP, getDyeColor(0xECC88C));
        put(Items.AIRSHIP, getDyeColor(0xECC88C));
        put(Items.CARGO_AIRSHIP, getDyeColor(0xECC88C));
    }};

    public static ItemColor getDyeColor(int defaultColor) {
        return (item, layer) -> {
            if (layer != 0) {
                return -1;
            } else if (item.getItem() instanceof DyeableLeatherItem dyeable && dyeable.hasCustomColor(item)) {
                return dyeable.getColor(item);
            } else {
                return defaultColor;
            }
        };
    }
}
