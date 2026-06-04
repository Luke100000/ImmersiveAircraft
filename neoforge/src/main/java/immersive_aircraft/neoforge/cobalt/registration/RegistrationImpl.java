package immersive_aircraft.neoforge.cobalt.registration;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.neoforge.NeoForgeBusEvents;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.Supplier;

public class RegistrationImpl extends Registration.Impl {
    private final Map<String, RegistryRepo> repos = new HashMap<>();
    private final DataLoaderRegister dataLoaderRegister = new DataLoaderRegister();
    private final DataLoaderRegister resourceLoaderRegister = new DataLoaderRegister();
    private final IEventBus modEventBus;

    public RegistrationImpl(IEventBus modEventBus) {
        this.modEventBus = modEventBus;
        NeoForgeBusEvents.DATA_REGISTRY = dataLoaderRegister;
        NeoForgeBusEvents.RESOURCE_REGISTRY = resourceLoaderRegister;
    }

    private RegistryRepo getRepo(String namespace) {
        return repos.computeIfAbsent(namespace, id -> new RegistryRepo(id, modEventBus));
    }

    @Override
    public <T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererProvider<T> constructor) {
        EntityRenderers.register(type, constructor);
    }

    @Override
    public void registerDataLoader(Identifier id, PreparableReloadListener loader) {
        dataLoaderRegister.add(id, loader);
    }

    @Override
    public void registerResourceLoader(Identifier id, PreparableReloadListener loader) {
        resourceLoaderRegister.add(id, loader);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj) {
        DeferredRegister reg = getRepo(id.getNamespace()).get(registry);
        return reg.register(id.getPath(), obj);
    }

    static class RegistryRepo {
        private final Map<Identifier, DeferredRegister<?>> registries = new HashMap<>();
        private final String namespace;
        private final IEventBus modEventBus;

        public RegistryRepo(String namespace, IEventBus modEventBus) {
            this.namespace = namespace;
            this.modEventBus = modEventBus;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> DeferredRegister get(Registry<? super T> registry) {
            Identifier id = registry.key().identifier();
            if (!registries.containsKey(id)) {
                DeferredRegister def = DeferredRegister.create(registry.key(), namespace);
                def.register(modEventBus);
                registries.put(id, def);
            }

            return registries.get(id);
        }
    }

    public static class DataLoaderRegister {
        private final List<Entry> dataLoaders = new ArrayList<>();

        public void add(Identifier id, PreparableReloadListener loader) {
            dataLoaders.add(new Entry(id, loader));
        }

        public List<PreparableReloadListener> getLoaders() {
            return dataLoaders.stream().map(Entry::loader).toList();
        }

        public List<Entry> getEntries() {
            return dataLoaders;
        }

        public record Entry(Identifier id, PreparableReloadListener loader) {
        }
    }
}
