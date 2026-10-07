package immersive_aircraft.client.compat;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.contextual.ContextualDomain;
import dev.isxander.controlify.api.contextual.InGameContext;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import dev.isxander.controlify.bindings.BindContext;
import immersive_aircraft.client.KeyBindings;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ControlifyGuide implements ControlifyEntrypoint {
    @Override
    public void onControlifyPreInit(PreInitContext context) {
        ContextualDomain<InGameContext> registry = context.contextualDomains().inGame();
        for (KeyMapping key : KeyBindings.list) {
            // NeoForge's key discovery can include modded keys in its vanilla snapshot.
            if (context.bindings().getKeyCorrelation(key).isEmpty()) {
                context.bindings().registerBinding(builder -> builder
                        .id("controlify_modded", key.getName())
                        .name(Component.translatable(key.getName()))
                        .category(key.getCategory().label())
                        .allowedContexts(BindContext.IN_GAME)
                        .keyEmulation(key));
            }
            register(registry, key);
        }
    }

    @Override
    public void onControlifyInit(InitContext context) {
    }

    @Override
    public void onControllersDiscovered(ControlifyApi controlify) {
    }

    private static void register(ContextualDomain<InGameContext> registry, KeyMapping key) {
        String translation = key.getName();
        String name = key.getName().replace("multi_", "").replace("fallback_", "");
        Identifier id = Identifier.fromNamespaceAndPath("immersive_aircraft", name);
        Identifier fallbackId = Identifier.fromNamespaceAndPath("immersive_aircraft", name + "/fallback");
        Identifier triggerId = Identifier.fromNamespaceAndPath("immersive_aircraft", name + "/trigger");
        registry.registerContributor((context, sink) -> {
            boolean active = translation.equals(AircraftGuide.label(key, context.player()));
            sink.contributeFact(id, active);
            sink.contributeFact(fallbackId, active && AircraftGuide.binding(key, context.player()) == key
                                           && KeyBindings.getFallbackKey(key) != null && !hasOwnBinding(key, context));
            if (key == KeyBindings.throttleUp) {
                sink.contributeFact(triggerId, active && AircraftGuide.binding(key, context.player()) != key);
            }
        });
    }

    private static boolean hasOwnBinding(KeyMapping key, InGameContext context) {
        return ControlifyBindApi.get().getKeyCorrelation(key).stream().anyMatch(supplier -> {
            InputBinding binding = supplier.onOrNull(context.controller());
            return binding != null && !binding.isUnbound();
        });
    }
}
