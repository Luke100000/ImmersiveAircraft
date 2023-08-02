package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public interface Entities {
    List<Supplier<Object>> REGISTRY = new LinkedList<>();
    Set<Object> REGISTRY_SET = new HashSet<>();

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
        Supplier<EntityType<T>> register = Registration.register(Registry.ENTITY_TYPE, id, () -> builder.build(id.toString()));
        REGISTRY.add(register::get);
        return register;
    }


    static boolean hasType(EntityType<?> type) {
        if (REGISTRY_SET.isEmpty()) {
            REGISTRY.forEach(v -> REGISTRY_SET.add(v.get()));
        }
        return REGISTRY_SET.contains(type);
    }
}
