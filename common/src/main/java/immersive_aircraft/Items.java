package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.entity.AirshipEntity;
import immersive_aircraft.entity.BiplaneEntity;
import immersive_aircraft.entity.GyrodyneEntity;
import immersive_aircraft.item.AircraftItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public interface Items {
    List<Supplier<Item>> items = new LinkedList<>();

    Supplier<Item> HULL = register("hull", () -> new Item(baseProps().maxCount(8)));
    Supplier<Item> ENGINE = register("engine", () -> new Item(baseProps().maxCount(8)));
    Supplier<Item> SAIL = register("sail", () -> new Item(baseProps().maxCount(8)));
    Supplier<Item> PROPELLER = register("propeller", () -> new Item(baseProps().maxCount(8)));
    Supplier<Item> BOILER = register("boiler", () -> new Item(baseProps().maxCount(8)));

    Supplier<Item> AIRSHIP = register("airship", () -> new AircraftItem(baseProps().maxCount(1), (world) -> new AirshipEntity(Entities.AIRSHIP.get(), world)));
    Supplier<Item> BIPLANE = register("biplane", () -> new AircraftItem(baseProps().maxCount(1), (world) -> new BiplaneEntity(Entities.BIPLANE.get(), world)));
    Supplier<Item> GYRODYNE = register("gyrodyne", () -> new AircraftItem(baseProps().maxCount(1), (world) -> new GyrodyneEntity(Entities.GYRODYNE.get(), world)));
    Supplier<Item> QUADROCOPTER = register("quadrocopter", () -> new AircraftItem(baseProps().maxCount(1), (world) -> new GyrodyneEntity(Entities.QUADROCOPTER.get(), world)));

    static Supplier<Item> register(String name, Supplier<Item> item) {
        Supplier<Item> register = Registration.register(Registries.ITEM, Main.locate(name), item);
        items.add(register);
        return register;
    }

    static void bootstrap() {
    }

    static Item.Settings baseProps() {
        return new Item.Settings();
    }

    static List<ItemStack> getSortedItems() {
        return items.stream().map(i -> i.get().getDefaultStack()).toList();
    }
}
