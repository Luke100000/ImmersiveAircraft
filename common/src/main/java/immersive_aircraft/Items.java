package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.entity.AirshipEntity;
import immersive_aircraft.entity.BiplaneEntity;
import immersive_aircraft.entity.GyrodyneEntity;
import immersive_aircraft.entity.QuadrocopterEntity;
import immersive_aircraft.item.AircraftItem;
import immersive_aircraft.item.UpgradeItem;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
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
    Supplier<Item> QUADROCOPTER = register("quadrocopter", () -> new AircraftItem(baseProps().maxCount(1), (world) -> new QuadrocopterEntity(Entities.QUADROCOPTER.get(), world)));

    Supplier<Item> ENHANCED_PROPELLER = register("enhanced_propeller", () -> new UpgradeItem(baseProps().maxCount(8), new AircraftUpgrade().set(AircraftStat.FRICTION, -0.75f)));
    Supplier<Item> ECO_ENGINE = register("eco_engine", () -> new UpgradeItem(baseProps().maxCount(8), new AircraftUpgrade().set(AircraftStat.STRENGTH, -0.2f).set(AircraftStat.FUEL, -0.4f)));
    Supplier<Item> NETHER_ENGINE = register("nether_engine", () -> new UpgradeItem(baseProps().maxCount(8), new AircraftUpgrade().set(AircraftStat.STRENGTH, 0.4f).set(AircraftStat.FUEL, 0.3f)));
    Supplier<Item> STEEL_BOILER = register("steel_boiler", () -> new UpgradeItem(baseProps().maxCount(8), new AircraftUpgrade().set(AircraftStat.STRENGTH, 0.25f).set(AircraftStat.FUEL, 0.5f)));
    Supplier<Item> INDUSTRIAL_GEARS = register("industrial_gears", () -> new UpgradeItem(baseProps().maxCount(8), new AircraftUpgrade().set(AircraftStat.FUEL, -0.2f)));
    Supplier<Item> STURDY_PIPES = register("sturdy_pipes", () -> new UpgradeItem(baseProps().maxCount(8), new AircraftUpgrade().set(AircraftStat.STRENGTH, 0.1f)));
    Supplier<Item> GYROSCOPE = register("gyroscope", () -> new UpgradeItem(baseProps().maxCount(8), new AircraftUpgrade().set(AircraftStat.WIND, -1.0f)));
    Supplier<Item> HULL_REINFORCEMENT = register("hull_reinforcement", () -> new UpgradeItem(baseProps().maxCount(8), new AircraftUpgrade().set(AircraftStat.DURABILITY, 1.0f)));
    Supplier<Item> IMPROVED_LANDING_GEAR = register("improved_landing_gear", () -> new UpgradeItem(baseProps().maxCount(8), new AircraftUpgrade().set(AircraftStat.ACCELERATION, 0.5f)));

    static Supplier<Item> register(String name, Supplier<Item> item) {
        return Registration.register(Registry.ITEM, Main.locate(name), item);
    }

    static void bootstrap() {
    }

    static Item.Settings baseProps() {
        return new Item.Settings().group(ItemGroups.GROUP);
    }
}
