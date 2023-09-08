package immersive_aircraft.cobalt.registration;

import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registry;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class Registration {
    private static Impl INSTANCE;

    public static <T extends AircraftEntity> void register(EntityType<?> type, EntityRendererFactory<T> constructor) {
        //noinspection unchecked
        INSTANCE.registerEntityRenderer((EntityType<T>) type, constructor);
    }

    public static <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj) {
        return INSTANCE.register(registry, id, obj);
    }

    public static void registerDataLoader(String id, JsonDataLoader loader) {
        INSTANCE.registerDataLoader(Main.locate(id), loader);
    }

    public static abstract class Impl {
        protected Impl() {
            INSTANCE = this;
        }

        public abstract <T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererFactory<T> constructor);

        public abstract void registerDataLoader(Identifier id, JsonDataLoader loader);

        public abstract <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj);
    }
}
