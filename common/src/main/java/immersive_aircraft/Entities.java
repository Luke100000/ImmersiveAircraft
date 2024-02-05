package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.entity.*;
import immersive_aircraft.entity.bullet.BulletEntity;
import immersive_aircraft.entity.bullet.TinyTNT;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public interface Entities {
    Supplier<EntityType<GyrodyneEntity>> GYRODYNE = register("gyrodyne", EntityType.Builder
            .of(GyrodyneEntity::new, MobCategory.MISC)
            .sized(1.25f, 0.6f)
            .clientTrackingRange(12)
            .fireImmune()
    );

    Supplier<EntityType<BiplaneEntity>> BIPLANE = register("biplane", EntityType.Builder
            .of(BiplaneEntity::new, MobCategory.MISC)
            .sized(1.75f, 0.85f)
            .clientTrackingRange(12)
            .fireImmune()
    );

    Supplier<EntityType<AirshipEntity>> AIRSHIP = register("airship", EntityType.Builder
            .of(AirshipEntity::new, MobCategory.MISC)
            .sized(1.5f, 2.5f)
            .clientTrackingRange(12)
            .fireImmune()
    );

    Supplier<EntityType<CargoAirshipEntity>> CARGO_AIRSHIP = register("cargo_airship", EntityType.Builder
            .of(CargoAirshipEntity::new, MobCategory.MISC)
            .sized(1.75f, 2.5f)
            .clientTrackingRange(12)
            .fireImmune()
    );

    Supplier<EntityType<QuadrocopterEntity>> QUADROCOPTER = register("quadrocopter", EntityType.Builder
            .of(QuadrocopterEntity::new, MobCategory.MISC)
            .sized(1.5f, 0.5f)
            .clientTrackingRange(10)
            .fireImmune()
    );

    Supplier<EntityType<BlimpEntity>> BLIMP = register("blimp", EntityType.Builder
            .of(BlimpEntity::new, MobCategory.MISC)
            .sized(8.0f, 8.0f)
            .clientTrackingRange(12)
            .fireImmune()
    );

    Supplier<EntityType<BulletEntity>> BULLET = register("bullet", EntityType.Builder
            .of(BulletEntity::new, MobCategory.MISC)
            .sized(0.25f, 0.25f)
            .clientTrackingRange(6)
            .updateInterval(10)
            .fireImmune()
    );

    Supplier<EntityType<TinyTNT>> TINY_TNT = register("tiny_tnt", EntityType.Builder
            .of(TinyTNT::new, MobCategory.MISC)
            .sized(0.375f, 0.375f)
            .clientTrackingRange(6)
            .updateInterval(10)
            .fireImmune()
    );

    static void bootstrap() {

    }

    static <T extends Entity> Supplier<EntityType<T>> register(String name, EntityType.Builder<T> builder) {
        ResourceLocation id = new ResourceLocation(Main.MOD_ID, name);
        return Registration.register(Registry.ENTITY_TYPE, id, () -> builder.build(id.toString()));
    }
}
