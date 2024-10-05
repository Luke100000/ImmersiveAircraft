package immersive_aircraft.neoforge.cobalt.registration;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.neoforge.NeoForgeBusEvents;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.Supplier;

/**
 * Contains all the crap required to interface with forge's code
 */
public class RegistrationImpl extends Registration.Impl {
    private final Map<String, RegistryRepo> repos = new HashMap<>();
    private final DataLoaderRegister dataLoaderRegister = new DataLoaderRegister();
    private final DataLoaderRegister resourceLoaderRegister = new DataLoaderRegister();

    private final IEventBus modBus;

    public RegistrationImpl(IEventBus modBus) {
        NeoForgeBusEvents.DATA_REGISTRY = dataLoaderRegister;
        NeoForgeBusEvents.RESOURCE_REGISTRY = resourceLoaderRegister;

        this.modBus = modBus;
    }

    private RegistryRepo getRepo(String namespace) {
        return repos.computeIfAbsent(namespace, RegistryRepo::new);
    }

    @Override
    public <T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererProvider<T> constructor) {
        EntityRenderers.register(type, constructor);
    }

    @Override
    public void registerDataLoader(ResourceLocation id, PreparableReloadListener loader) {
        dataLoaderRegister.dataLoaders.add(loader);
    }

    @Override
    public void registerResourceLoader(ResourceLocation id, PreparableReloadListener loader) {
        resourceLoaderRegister.dataLoaders.add(loader);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> obj) {
        DeferredRegister reg = getRepo(id.getNamespace()).get(registry);
        return reg.register(id.getPath(), obj);
    }

    class RegistryRepo {
        private final Set<ResourceLocation> skipped = new HashSet<>();
        private final Map<ResourceLocation, DeferredRegister<?>> registries = new HashMap<>();

        private final String namespace;

        public RegistryRepo(String namespace) {
            this.namespace = namespace;
        }

        @SuppressWarnings({"rawtypes"})
        public <T> DeferredRegister get(Registry<? super T> registry) {
            ResourceLocation id = registry.key().location();
            if (!registries.containsKey(id) && !skipped.contains(id)) {
                DeferredRegister def = DeferredRegister.create(registry, namespace);

                def.register(modBus);

                registries.put(id, def);
            }

            return registries.get(id);
        }
    }

    public static class DataLoaderRegister {
        // Doing no setter means only the RegistrationImpl class can get access to registering more loaders.
        private final List<PreparableReloadListener> dataLoaders = new ArrayList<>();

        public List<PreparableReloadListener> getLoaders() {
            return dataLoaders;
        }
    }
}
