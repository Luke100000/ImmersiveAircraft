package immersive_aircraft.forge.cobalt.registration;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.forge.ForgeBusEvents;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.Supplier;

/**
 * Contains all the crap required to interface with forge's code
 */
public class RegistrationImpl extends Registration.Impl {
    private final Map<String, RegistryRepo> repos = new HashMap<>();
    private final DataLoaderRegister dataLoaderRegister = new DataLoaderRegister();
    private final DataLoaderRegister resourceLoaderRegister = new DataLoaderRegister();
    private final BusGroup modEventBus;

    public RegistrationImpl(BusGroup modEventBus) {
        this.modEventBus = modEventBus;
        ForgeBusEvents.DATA_REGISTRY = dataLoaderRegister;
        ForgeBusEvents.RESOURCE_REGISTRY = resourceLoaderRegister;
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
        dataLoaderRegister.dataLoaders.add(loader);
    }

    @Override
    public void registerResourceLoader(Identifier id, PreparableReloadListener loader) {
        resourceLoaderRegister.dataLoaders.add(loader);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj) {
        DeferredRegister reg = getRepo(id.getNamespace()).get(registry);
        return reg.register(id.getPath(), obj);
    }

    static class RegistryRepo {
        private final Set<Identifier> skipped = new HashSet<>();
        private final Map<Identifier, DeferredRegister<?>> registries = new HashMap<>();

        private final String namespace;
        private final BusGroup modEventBus;

        public RegistryRepo(String namespace, BusGroup modEventBus) {
            this.namespace = namespace;
            this.modEventBus = modEventBus;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> DeferredRegister get(Registry<? super T> registry) {
            Identifier id = registry.key().identifier();
            if (!registries.containsKey(id) && !skipped.contains(id)) {
                DeferredRegister def = DeferredRegister.create(registry.key(), namespace);

                def.register(modEventBus);

                registries.put(id, def);
            }

            return registries.get(id);
        }
    }

    public static class DataLoaderRegister {
        private final List<PreparableReloadListener> dataLoaders = new ArrayList<>(); // Doing no setter means only the RegistrationImpl class can get access to registering more loaders.

        public List<PreparableReloadListener> getLoaders() {
            return dataLoaders;
        }
    }
}
