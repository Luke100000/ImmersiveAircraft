package immersive_aircraft.neoforge.cobalt.registration;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.neoforge.NeoForgeBusEvents;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
    public void registerDataLoader(Identifier id, PreparableReloadListener loader) {
        dataLoaderRegister.dataLoaders.put(id, loader);
    }

    @Override
    public void registerResourceLoader(Identifier id, PreparableReloadListener loader) {
        resourceLoaderRegister.dataLoaders.put(id, loader);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj) {
        DeferredRegister reg = getRepo(id.getNamespace()).get(registry);
        return reg.register(id.getPath(), () -> {
            Registration.pushId(id);
            try {
                return obj.get();
            } finally {
                Registration.clearId();
            }
        });
    }

    class RegistryRepo {
        private final Map<Identifier, DeferredRegister<?>> registries = new HashMap<>();

        private final String namespace;

        public RegistryRepo(String namespace) {
            this.namespace = namespace;
        }

        @SuppressWarnings({"rawtypes"})
        public <T> DeferredRegister get(Registry<? super T> registry) {
            Identifier id = registry.key().identifier();
            if (!registries.containsKey(id)) {
                DeferredRegister def = DeferredRegister.create(registry, namespace);

                def.register(modBus);

                registries.put(id, def);
            }

            return registries.get(id);
        }
    }

    public static class DataLoaderRegister {
        // Doing no setter means only the RegistrationImpl class can get access to registering more loaders.
        private final Map<Identifier, PreparableReloadListener> dataLoaders = new LinkedHashMap<>();

        public Map<Identifier, PreparableReloadListener> getLoaders() {
            return dataLoaders;
        }
    }
}

