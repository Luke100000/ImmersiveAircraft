package immersive_aircraft.cobalt.registration;

import immersive_aircraft.Main;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

public class Registration {
    private static Impl INSTANCE;

    public static <T extends Entity> void register(EntityType<T> type, EntityRendererProvider<T> constructor) {
        INSTANCE.registerEntityRenderer(type, constructor);
    }

    public static <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj) {
        return INSTANCE.register(registry, id, obj);
    }

    public static void registerDataLoader(String id, PreparableReloadListener loader) {
        INSTANCE.registerDataLoader(Main.locate(id), loader);
    }

    public static void registerResourceLoader(String id, PreparableReloadListener loader) {
        INSTANCE.registerResourceLoader(Main.locate(id), loader);
    }

    public abstract static class Impl {
        protected Impl() {
            INSTANCE = this;
        }

        public abstract <T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererProvider<T> constructor);

        public abstract void registerDataLoader(Identifier id, PreparableReloadListener loader);

        public abstract void registerResourceLoader(Identifier id, PreparableReloadListener loader);

        public abstract <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj);
    }
}
