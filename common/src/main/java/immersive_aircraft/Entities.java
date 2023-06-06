package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public interface Entities {
    Supplier<EntityType<GyrodyneEntity>> GYRODYNE = register("gyrodyne", EntityType.Builder
            .create(GyrodyneEntity::new, SpawnGroup.MISC)
            .setDimensions(1.25f, 0.6f)
            .makeFireImmune()
    );

    Supplier<EntityType<BiplaneEntity>> BIPLANE = register("biplane", EntityType.Builder
            .create(BiplaneEntity::new, SpawnGroup.MISC)
            .setDimensions(1.75f, 0.85f)
            .makeFireImmune()
    );

    Supplier<EntityType<AirshipEntity>> AIRSHIP = register("airship", EntityType.Builder
            .create(AirshipEntity::new, SpawnGroup.MISC)
            .setDimensions(1.5f, 2.5f)
            .makeFireImmune()
    );

    Supplier<EntityType<CargoAirshipEntity>> CARGO_AIRSHIP = register("cargo_airship", EntityType.Builder
            .create(CargoAirshipEntity::new, SpawnGroup.MISC)
            .setDimensions(1.75f, 2.5f)
            .makeFireImmune()
    );

    Supplier<EntityType<QuadrocopterEntity>> QUADROCOPTER = register("quadrocopter", EntityType.Builder
            .create(QuadrocopterEntity::new, SpawnGroup.MISC)
            .setDimensions(1.5f, 0.5f)
            .makeFireImmune()
    );

    static void bootstrap() {

    }

    static <T extends Entity> Supplier<EntityType<T>> register(String name, EntityType.Builder<T> builder) {
        Identifier id = new Identifier(Main.MOD_ID, name);
        return Registration.register(Registry.ENTITY_TYPE, id, () -> builder.build(id.toString()));
    }
}
