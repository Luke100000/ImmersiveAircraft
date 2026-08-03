package immersive_aircraft;

import immersive_aircraft.entity.AirshipEntity;
import immersive_aircraft.entity.BambooHopperEntity;
import immersive_aircraft.entity.BiplaneEntity;
import immersive_aircraft.entity.CargoAirshipEntity;
import immersive_aircraft.entity.GyrodyneEntity;
import immersive_aircraft.entity.QuadrocopterEntity;
import immersive_aircraft.entity.WarshipEntity;
import net.minecraftforge.fml.common.registry.EntityRegistry;

/**
 * 1.12.2 port: entities are registered directly with EntityRegistry during preInit
 * (1.12.2 has no entity RegistryEvent worth using here). Hitbox sizes and fire
 * immunity move into the entity constructors (Entity.setSize / isImmuneToFire).
 */
public class Entities {
    private static int nextId = 0;

    public static void bootstrap() {
        register("gyrodyne", GyrodyneEntity.class);
        register("biplane", BiplaneEntity.class);
        register("airship", AirshipEntity.class);
        register("cargo_airship", CargoAirshipEntity.class);
        register("quadrocopter", QuadrocopterEntity.class);
        register("warship", WarshipEntity.class);
        register("bamboo_hopper", BambooHopperEntity.class);
        register("bullet", immersive_aircraft.entity.bullet.BulletEntity.class);
        register("tiny_tnt", immersive_aircraft.entity.bullet.TinyTNT.class);
    }

    private static void register(String name, Class<? extends net.minecraft.entity.Entity> entityClass) {
        EntityRegistry.registerModEntity(Main.locate(name), entityClass, Main.MOD_ID + "." + name, nextId++, Main.instance, 64, 3, true);
    }
}
