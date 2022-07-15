package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.entity.BiplaneEntity;
import immersive_aircraft.entity.GyrodyneEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface Entities {
    EntityType<GyrodyneEntity> GYRODYNE = register("gyrodyne", EntityType.Builder
            .create(GyrodyneEntity::new, SpawnGroup.MISC)
            .setDimensions(1.25f, 1.0f)
            .makeFireImmune()
    );

    EntityType<BiplaneEntity> BIPLANE = register("biplane", EntityType.Builder
            .create(BiplaneEntity::new, SpawnGroup.MISC)
            .setDimensions(1.75f, 0.85f)
            .makeFireImmune()
    );

    static void bootstrap() {

    }

    static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        Identifier id = new Identifier(Main.MOD_ID, name);
        return Registration.register(Registry.ENTITY_TYPE, id, builder.build(id.toString()));
    }
}
