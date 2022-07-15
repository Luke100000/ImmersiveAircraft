package immersive_airships;

import immersive_airships.cobalt.registration.Registration;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public interface Items {
    static Item register(String name, Item item) {
        return Registration.register(Registry.ITEM, Main.locate(name), item);
    }

    static void bootstrap() {
    }

    static Item.Settings baseProps() {
        return new Item.Settings().group(ItemGroups.GROUP);
    }
}
