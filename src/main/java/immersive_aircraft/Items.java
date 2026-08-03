package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.entity.AirshipEntity;
import immersive_aircraft.entity.BambooHopperEntity;
import immersive_aircraft.entity.BiplaneEntity;
import immersive_aircraft.entity.CargoAirshipEntity;
import immersive_aircraft.entity.GyrodyneEntity;
import immersive_aircraft.entity.QuadrocopterEntity;
import immersive_aircraft.entity.WarshipEntity;
import immersive_aircraft.item.AircraftItem;
import immersive_aircraft.item.DyeableAircraftItem;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.item.UpgradeItem;
import immersive_aircraft.item.WeaponItem;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import java.util.function.Supplier;

public interface Items {
    Supplier<Item> HULL = register("hull", () -> new Item().setMaxStackSize(8));
    Supplier<Item> ENGINE = register("engine", () -> new Item().setMaxStackSize(8));
    Supplier<Item> SAIL = register("sail", () -> new Item().setMaxStackSize(8));
    Supplier<Item> PROPELLER = register("propeller", () -> new Item().setMaxStackSize(8));
    Supplier<Item> BOILER = register("boiler", () -> new Item().setMaxStackSize(8));

    Supplier<Item> AIRSHIP = register("airship", () -> new AircraftItem(AirshipEntity::new));
    Supplier<Item> CARGO_AIRSHIP = register("cargo_airship", () -> new AircraftItem(CargoAirshipEntity::new));
    Supplier<Item> BIPLANE = register("biplane", () -> new AircraftItem(BiplaneEntity::new));
    Supplier<Item> GYRODYNE = register("gyrodyne", () -> new AircraftItem(GyrodyneEntity::new));
    Supplier<Item> QUADROCOPTER = register("quadrocopter", () -> new AircraftItem(QuadrocopterEntity::new));
    Supplier<Item> WARSHIP = register("warship", () -> new DyeableAircraftItem(WarshipEntity::new));
    Supplier<Item> BAMBOO_HOPPER = register("bamboo_hopper", () -> new AircraftItem(BambooHopperEntity::new));

    Supplier<Item> ROTARY_CANNON = register("rotary_cannon", () -> new WeaponItem(WeaponMount.Type.ROTATING));
    Supplier<Item> HEAVY_CROSSBOW = register("heavy_crossbow", () -> new WeaponItem(WeaponMount.Type.FRONT));
    Supplier<Item> TELESCOPE = register("telescope", () -> new WeaponItem(WeaponMount.Type.ROTATING));
    Supplier<Item> BOMB_BAY = register("bomb_bay", () -> new WeaponItem(WeaponMount.Type.DROP));

    Supplier<Item> ENHANCED_PROPELLER = register("enhanced_propeller", () -> new UpgradeItem(new AircraftUpgrade().set(AircraftStat.FRICTION, -0.75f)));
    Supplier<Item> ECO_ENGINE = register("eco_engine", () -> new UpgradeItem(new AircraftUpgrade().set(AircraftStat.STRENGTH, -0.2f).set(AircraftStat.FUEL, -0.75f)));
    Supplier<Item> NETHER_ENGINE = register("nether_engine", () -> new UpgradeItem(new AircraftUpgrade().set(AircraftStat.STRENGTH, 0.4f).set(AircraftStat.FUEL, 0.3f)));
    Supplier<Item> STEEL_BOILER = register("steel_boiler", () -> new UpgradeItem(new AircraftUpgrade().set(AircraftStat.STRENGTH, 0.25f).set(AircraftStat.FUEL, 0.5f)));
    Supplier<Item> INDUSTRIAL_GEARS = register("industrial_gears", () -> new UpgradeItem(new AircraftUpgrade().set(AircraftStat.FUEL, -0.2f)));
    Supplier<Item> STURDY_PIPES = register("sturdy_pipes", () -> new UpgradeItem(new AircraftUpgrade().set(AircraftStat.STRENGTH, 0.1f)));
    Supplier<Item> GYROSCOPE = register("gyroscope", () -> new UpgradeItem(new AircraftUpgrade().set(AircraftStat.WIND, -1.0f)));
    // 1.20.1: also wind -1.0 + stabilizer 0.03 (no stabilizer stat in this codebase) + HUD/dials
    // effect - TODO(stage-5b): the HUD indicators of the 1.20.1 overlay are not ported
    Supplier<Item> GYROSCOPE_HUD = register("gyroscope_hud", () -> new UpgradeItem(new AircraftUpgrade().set(AircraftStat.WIND, -1.0f)));
    Supplier<Item> GYROSCOPE_DIALS = register("gyroscope_dials", () -> new UpgradeItem(new AircraftUpgrade().set(AircraftStat.WIND, -1.0f)));
    Supplier<Item> HULL_REINFORCEMENT = register("hull_reinforcement", () -> new UpgradeItem(new AircraftUpgrade().set(AircraftStat.DURABILITY, 1.0f)));
    Supplier<Item> IMPROVED_LANDING_GEAR = register("improved_landing_gear", () -> new UpgradeItem(new AircraftUpgrade().set(AircraftStat.ACCELERATION, 0.5f)));

    static Supplier<Item> register(String name, Supplier<Item> item) {
        return Registration.register(name, item);
    }

    static void bootstrap() {
    }
}
