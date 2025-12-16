package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.entity.*;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.item.AircraftItem;
import immersive_aircraft.item.DyeableAircraftItem;
import immersive_aircraft.item.WeaponItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Items {
    List<Supplier<Item>> items = new LinkedList<>();

    Supplier<Item> HULL = register("hull", Item::new, baseProps().stacksTo(8));
    Supplier<Item> ENGINE = register("engine", Item::new, baseProps().stacksTo(8));
    Supplier<Item> SAIL = register("sail", Item::new, baseProps().stacksTo(8));
    Supplier<Item> PROPELLER = register("propeller", Item::new, baseProps().stacksTo(8));
    Supplier<Item> BOILER = register("boiler", Item::new, baseProps().stacksTo(8));

    Supplier<Item> AIRSHIP = register("airship",
            (props) -> new DyeableAircraftItem(props, world -> new AirshipEntity(Entities.AIRSHIP.get(), world)),
            baseProps().stacksTo(1));
    Supplier<Item> CARGO_AIRSHIP = register("cargo_airship",
            (props) -> new DyeableAircraftItem(props, world -> new CargoAirshipEntity(Entities.CARGO_AIRSHIP.get(), world)),
            baseProps().stacksTo(1));
    Supplier<Item> WARSHIP = register("warship",
            (props) -> new DyeableAircraftItem(props, world -> new WarshipEntity(Entities.WARSHIP.get(), world)),
            baseProps().stacksTo(1));
    Supplier<Item> BIPLANE = register("biplane",
            (props) -> new AircraftItem(props, world -> new BiplaneEntity(Entities.BIPLANE.get(), world)),
            baseProps().stacksTo(1));
    Supplier<Item> GYRODYNE = register("gyrodyne",
            (props) -> new AircraftItem(props, world -> new GyrodyneEntity(Entities.GYRODYNE.get(), world)),
            baseProps().stacksTo(1)
    );
    Supplier<Item> QUADROCOPTER = register("quadrocopter",
            (props) -> new AircraftItem(props, world -> new QuadrocopterEntity(Entities.QUADROCOPTER.get(), world)),
            baseProps().stacksTo(1));
    Supplier<Item> BAMBOO_HOPPER = register("bamboo_hopper",
            (props) -> new AircraftItem(props, world -> new BambooHopperEntity(Entities.BAMBOO_HOPPER.get(), world)),
            baseProps().stacksTo(1));

    Supplier<Item> ROTARY_CANNON = register("rotary_cannon",
            (props) -> new WeaponItem(props, WeaponMount.Type.ROTATING), baseProps().stacksTo(1));
    Supplier<Item> HEAVY_CROSSBOW = register("heavy_crossbow",
            (props) -> new WeaponItem(props, WeaponMount.Type.FRONT), baseProps().stacksTo(1));
    Supplier<Item> TELESCOPE = register("telescope",
            (props) -> new WeaponItem(props, WeaponMount.Type.ROTATING), baseProps().stacksTo(1));
    Supplier<Item> BOMB_BAY = register("bomb_bay",
            (props) -> new WeaponItem(props, WeaponMount.Type.DROP), baseProps().stacksTo(1));

    Supplier<Item> ENHANCED_PROPELLER = register("enhanced_propeller",
            Item::new, baseProps().stacksTo(8));
    Supplier<Item> ECO_ENGINE = register("eco_engine",
            Item::new, baseProps().stacksTo(8));
    Supplier<Item> NETHER_ENGINE = register("nether_engine",
            Item::new, baseProps().stacksTo(8));
    Supplier<Item> STEEL_BOILER = register("steel_boiler",
            Item::new, baseProps().stacksTo(8));
    Supplier<Item> INDUSTRIAL_GEARS = register("industrial_gears",
            Item::new, baseProps().stacksTo(8));
    Supplier<Item> STURDY_PIPES = register("sturdy_pipes",
            Item::new, baseProps().stacksTo(8));
    Supplier<Item> GYROSCOPE = register("gyroscope",
            Item::new, baseProps().stacksTo(8));
    Supplier<Item> GYROSCOPE_HUD = register("gyroscope_hud",
            Item::new, baseProps().stacksTo(8));
    Supplier<Item> GYROSCOPE_DIALS = register("gyroscope_dials",
            Item::new, baseProps().stacksTo(8));
    Supplier<Item> HULL_REINFORCEMENT = register("hull_reinforcement",
            Item::new, baseProps().stacksTo(8));
    Supplier<Item> IMPROVED_LANDING_GEAR = register("improved_landing_gear",
            Item::new, baseProps().stacksTo(8));

    static Supplier<Item> register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties properties) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, name));
        Item item = itemFactory.apply(properties.setId(itemKey));
        Supplier<Item> register = Registration.register(BuiltInRegistries.ITEM, Main.locate(name), () -> item);
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
