package immersive_aircraft.cobalt.registration;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class ClientRegistration {
    private static Impl INSTANCE;

    private ClientRegistration() {
    }

    public static <T extends Entity> void register(EntityType<T> type, EntityRendererProvider<T> constructor) {
        INSTANCE.registerEntityRenderer(type, constructor);
    }

    public interface Impl {
        <T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererProvider<T> constructor);
    }

    public static void setImpl(Impl impl) {
        INSTANCE = impl;
    }
}
