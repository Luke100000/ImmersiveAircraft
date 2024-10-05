package immersive_aircraft;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.HashMap;
import java.util.Map;

public class ItemColors {
    public static Map<Item, ItemColor> ITEM_COLORS = new HashMap<>() {{
        put(Items.WARSHIP.get(), getDyeColor(0xFFECC88C));
        put(Items.AIRSHIP.get(), getDyeColor(0xFFECC88C));
        put(Items.CARGO_AIRSHIP.get(), getDyeColor(0xFFECC88C));
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
