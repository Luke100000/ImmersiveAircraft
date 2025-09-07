package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.entity.*;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.item.AircraftItem;
import immersive_aircraft.item.DyeableAircraftItem;
import immersive_aircraft.item.WeaponItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public interface Items {
    List<Supplier<Item>> items = new LinkedList<>();

    Supplier<Item> HULL = register("hull", () -> new Item(baseProps().stacksTo(8)));
    Supplier<Item> ENGINE = register("engine", () -> new Item(baseProps().stacksTo(8)));
    Supplier<Item> SAIL = register("sail", () -> new Item(baseProps().stacksTo(8)));
    Supplier<Item> PROPELLER = register("propeller", () -> new Item(baseProps().stacksTo(8)));
    Supplier<Item> BOILER = register("boiler", () -> new Item(baseProps().stacksTo(8)));

    Supplier<Item> AIRSHIP = register("airship", () -> new DyeableAircraftItem(baseProps().stacksTo(1), world -> new AirshipEntity(Entities.AIRSHIP.get(), world)));
    Supplier<Item> CARGO_AIRSHIP = register("cargo_airship", () -> new DyeableAircraftItem(baseProps().stacksTo(1), world -> new CargoAirshipEntity(Entities.CARGO_AIRSHIP.get(), world)));
    Supplier<Item> WARSHIP = register("warship", () -> new DyeableAircraftItem(baseProps().stacksTo(1), world -> new WarshipEntity(Entities.WARSHIP.get(), world)));
    Supplier<Item> BIPLANE = register("biplane", () -> new AircraftItem(baseProps().stacksTo(1), world -> new BiplaneEntity(Entities.BIPLANE.get(), world)));
    Supplier<Item> GYRODYNE = register("gyrodyne", () -> new AircraftItem(baseProps().stacksTo(1), world -> new GyrodyneEntity(Entities.GYRODYNE.get(), world)));
    Supplier<Item> QUADROCOPTER = register("quadrocopter", () -> new AircraftItem(baseProps().stacksTo(1), world -> new QuadrocopterEntity(Entities.QUADROCOPTER.get(), world)));
    Supplier<Item> BAMBOO_HOPPER = register("bamboo_hopper", () -> new AircraftItem(baseProps().stacksTo(1), world -> new BambooHopperEntity(Entities.BAMBOO_HOPPER.get(), world)));

    Supplier<Item> ROTARY_CANNON = register("rotary_cannon", () -> new WeaponItem(baseProps().stacksTo(1), WeaponMount.Type.ROTATING));
    Supplier<Item> HEAVY_CROSSBOW = register("heavy_crossbow", () -> new WeaponItem(baseProps().stacksTo(1), WeaponMount.Type.FRONT));
    Supplier<Item> TELESCOPE = register("telescope", () -> new WeaponItem(baseProps().stacksTo(1), WeaponMount.Type.ROTATING));
    Supplier<Item> BOMB_BAY = register("bomb_bay", () -> new WeaponItem(baseProps().stacksTo(1), WeaponMount.Type.DROP));

    Supplier<Item> ENHANCED_PROPELLER = register("enhanced_propeller", () -> new Item(baseProps().stacksTo(8)));
    Supplier<Item> ECO_ENGINE = register("eco_engine", () -> new Item(baseProps().stacksTo(8)));
    Supplier<Item> NETHER_ENGINE = register("nether_engine", () -> new Item(baseProps().stacksTo(8)));
    Supplier<Item> STEEL_BOILER = register("steel_boiler", () -> new Item(baseProps().stacksTo(8)));
    Supplier<Item> INDUSTRIAL_GEARS = register("industrial_gears", () -> new Item(baseProps().stacksTo(8)));
    Supplier<Item> STURDY_PIPES = register("sturdy_pipes", () -> new Item(baseProps().stacksTo(8)));
    Supplier<Item> GYROSCOPE = register("gyroscope", () -> new Item(baseProps().stacksTo(8)));
    Supplier<Item> GYROSCOPE_HUD = register("gyroscope_hud", () -> new Item(baseProps().stacksTo(8)));
    Supplier<Item> GYROSCOPE_DIALS = register("gyroscope_dials", () -> new Item(baseProps().stacksTo(8)));
    Supplier<Item> HULL_REINFORCEMENT = register("hull_reinforcement", () -> new Item(baseProps().stacksTo(8)));
    Supplier<Item> IMPROVED_LANDING_GEAR = register("improved_landing_gear", () -> new Item(baseProps().stacksTo(8)));

    static Supplier<Item> register(String name, Supplier<Item> item) {
        Supplier<Item> register = Registration.register(BuiltInRegistries.ITEM, Main.locate(name), item);
        items.add(register);
        return register;
    }

    static void bootstrap() {
    }

    static Item.Properties baseProps() {
        return new Item.Properties();
    }

    static List<ItemStack> getSortedItems() {
        return items.stream().map(i -> i.get().getDefaultInstance()).toList();
    }
}
