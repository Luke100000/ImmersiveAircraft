package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.entity.AirshipEntity;
import immersive_aircraft.entity.BiplaneEntity;
import immersive_aircraft.entity.GyrodyneEntity;
import immersive_aircraft.item.AircraftItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public interface Items {
    Supplier<Item> HULL = register("hull", () -> new Item(baseProps().maxCount(8)));
    Supplier<Item> ENGINE = register("engine", () -> new Item(baseProps().maxCount(8)));
    Supplier<Item> SAIL = register("sail", () -> new Item(baseProps().maxCount(8)));
    Supplier<Item> PROPELLER = register("propeller", () -> new Item(baseProps().maxCount(8)));
    Supplier<Item> BOILER = register("boiler", () -> new Item(baseProps().maxCount(8)));

    Supplier<Item> AIRSHIP = register("airship", () -> new AircraftItem(baseProps().maxCount(1), (world) -> new AirshipEntity(Entities.AIRSHIP.get(), world)));
    Supplier<Item> BIPLANE = register("biplane", () -> new AircraftItem(baseProps().maxCount(1), (world) -> new BiplaneEntity(Entities.BIPLANE.get(), world)));
    Supplier<Item> GYRODYNE = register("gyrodyne", () -> new AircraftItem(baseProps().maxCount(1), (world) -> new GyrodyneEntity(Entities.GYRODYNE.get(), world)));

    static Supplier<Item> register(String name, Supplier<Item> item) {
        return Registration.register(Registry.ITEM, Main.locate(name), item);
    }

    static void bootstrap() {
    }

    static Item.Settings baseProps() {
        return new Item.Settings().group(ItemGroups.GROUP);
    }
}
