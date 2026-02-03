package immersive_aircraft.cobalt.registration;

import immersive_aircraft.Main;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.function.Supplier;

public class Registration {
    private static Impl INSTANCE;
    private static final ThreadLocal<Identifier> CURRENT_ID = new ThreadLocal<>();

    public static <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj) {
        return INSTANCE.register(registry, id, obj);
    }

    public static Identifier currentId() {
        return CURRENT_ID.get();
    }

    public static void pushId(Identifier id) {
        CURRENT_ID.set(id);
    }

    public static void clearId() {
        CURRENT_ID.remove();
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

        public abstract void registerDataLoader(Identifier id, PreparableReloadListener loader);

        public abstract void registerResourceLoader(Identifier id, PreparableReloadListener loader);

        public abstract <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj);
    }
}

