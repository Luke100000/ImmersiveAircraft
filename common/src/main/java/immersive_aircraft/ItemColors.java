package immersive_aircraft;

import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ItemColors {
    // Placeholder map for item tint data; actual registration is disabled for 1.21.11
    public static Map<Supplier<Item>, Integer> ITEM_COLORS = new HashMap<>() {{
        put(Items.WARSHIP, 0xFFECC88C);
        put(Items.AIRSHIP, 0xFFECC88C);
        put(Items.CARGO_AIRSHIP, 0xFFECC88C);
    }};
}
