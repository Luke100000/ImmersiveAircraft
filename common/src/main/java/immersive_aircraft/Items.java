package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public interface Items {
    static Supplier<Item> register(String name, Supplier<Item> item) {
        return Registration.register(Registry.ITEM, Main.locate(name), item);
    }

    static void bootstrap() {
    }

    static Item.Settings baseProps() {
        return new Item.Settings().group(ItemGroups.GROUP);
    }
}
