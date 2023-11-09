package immersive_aircraft.cobalt.registration;

import immersive_aircraft.Main;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class Registration {
    private static Impl INSTANCE;

    public static <T extends Entity> void register(EntityType<T> type, EntityRendererProvider<T> constructor) {
        INSTANCE.registerEntityRenderer(type, constructor);
    }

    public static <T> Supplier<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> obj) {
        return INSTANCE.register(registry, id, obj);
    }

    public static class ObjectBuilders {
        public static class ItemGroups {
            public static CreativeModeTab create(ResourceLocation id, Supplier<ItemStack> icon) {
                return INSTANCE.itemGroup(id, icon);
            }
        }
    }

    public static void registerDataLoader(String id, SimpleJsonResourceReloadListener loader) {
        INSTANCE.registerDataLoader(Main.locate(id), loader);
    }

    public abstract static class Impl {
        protected Impl() {
            INSTANCE = this;
        }

        public abstract <T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererProvider<T> constructor);

        public abstract void registerDataLoader(ResourceLocation id, SimpleJsonResourceReloadListener loader);

        public abstract <T> Supplier<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> obj);

        public abstract CreativeModeTab itemGroup(ResourceLocation id, Supplier<ItemStack> icon);
    }
}
