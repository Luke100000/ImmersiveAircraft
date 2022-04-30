package immersive_airships;

import immersive_airships.cobalt.registration.Registration;
import immersive_airships.entity.AirshipEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface Entities {
    EntityType<AirshipEntity> AIRSHIP = register("airship", EntityType.Builder
            .<AirshipEntity>create(AirshipEntity::new, SpawnGroup.MISC)
            .setDimensions(1.25f, 1.0f)
            .makeFireImmune()
    );

    static void bootstrap() {

    }

    static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        Identifier id = new Identifier(Main.MOD_ID, name);
        return Registration.register(Registry.ENTITY_TYPE, id, builder.build(id.toString()));
    }
}
