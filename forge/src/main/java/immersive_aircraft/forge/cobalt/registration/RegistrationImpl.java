package immersive_aircraft.forge.cobalt.registration;

import immersive_aircraft.cobalt.registration.Registration;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.*;
import java.util.function.Supplier;

/**
 * Contains all the crap required to interface with forge's code
 */
public class RegistrationImpl extends Registration.Impl {
    @SuppressWarnings("unused")
    public static final RegistrationImpl IMPL = new RegistrationImpl();

    private final Map<String, RegistryRepo> repos = new HashMap<>();

    public static void bootstrap() {}

    private RegistryRepo getRepo(String namespace) {
        return repos.computeIfAbsent(namespace, RegistryRepo::new);
    }

    @Override
    public <T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererFactory<T> constructor) {
        EntityRenderers.register(type, constructor);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj) {
        DeferredRegister reg = getRepo(id.getNamespace()).get(registry);
        return reg.register(id.getPath(), obj);
    }

    @Override
    public ItemGroup itemGroup(Identifier id, Supplier<ItemStack> icon) {
        return new ItemGroup(ItemGroup.getGroupCountSafe(), String.format("%s.%s", id.getNamespace(), id.getPath())) {
            @Override
            public ItemStack createIcon() {
                return icon.get();
            }
        };
    }

    static class RegistryRepo {
        private final Set<Identifier> skipped = new HashSet<>();
        private final Map<Identifier, DeferredRegister<?>> registries = new HashMap<>();

        private final String namespace;

        public RegistryRepo(String namespace) {
            this.namespace = namespace;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public <T> DeferredRegister get(Registry<? super T> registry) {
            Identifier id = registry.getKey().getValue();
            if (!registries.containsKey(id) && !skipped.contains(id)) {
                ForgeRegistry reg = RegistryManager.ACTIVE.getRegistry(id);
                if (reg == null) {
                    skipped.add(id);
                    return null;
                }

                DeferredRegister def = DeferredRegister.create(Objects.requireNonNull(reg, "Registry=" + id), namespace);

                def.register(FMLJavaModLoadingContext.get().getModEventBus());

                registries.put(id, def);
            }

            return registries.get(id);
        }
    }
}
