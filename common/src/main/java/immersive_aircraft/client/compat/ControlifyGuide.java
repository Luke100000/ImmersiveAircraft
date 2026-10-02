package immersive_aircraft.client.compat;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import dev.isxander.controlify.api.guide.Fact;
import dev.isxander.controlify.api.guide.GuideDomainRegistry;
import dev.isxander.controlify.api.guide.InGameCtx;
import immersive_aircraft.client.KeyBindings;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

public final class ControlifyGuide implements ControlifyEntrypoint {
    @Override
    public void onControlifyPreInit(PreInitContext context) {
        GuideDomainRegistry<InGameCtx> registry = context.guideRegistries().inGame();
        for (KeyMapping key : KeyBindings.list) {
            register(registry, key, "", key.getName());
            if (key == KeyBindings.up || key == KeyBindings.down) {
                register(registry, key, "/throttle", key == KeyBindings.up
                        ? "guide.immersive_aircraft.throttle_up" : "guide.immersive_aircraft.throttle_down");
            }
        }
    }

    @Override
    public void onControlifyInit(InitContext context) {
    }

    @Override
    public void onControllersDiscovered(ControlifyApi controlify) {
    }

    private static void register(GuideDomainRegistry<InGameCtx> registry, KeyMapping key, String suffix, String translation) {
        String name = key.getName().replace("multi_", "").replace("fallback_", "") + suffix;
        registry.registerFact(Fact.of(ResourceLocation.fromNamespaceAndPath("immersive_aircraft", name),
                context -> translation.equals(AircraftGuide.label(key, context.player()))));
        registry.registerFact(Fact.of(ResourceLocation.fromNamespaceAndPath("immersive_aircraft", name + "/fallback"),
                context -> translation.equals(AircraftGuide.label(key, context.player()))
                        && KeyBindings.getFallbackKey(key) != null && !hasOwnBinding(key, context)));
    }

    private static boolean hasOwnBinding(KeyMapping key, InGameCtx context) {
        return ControlifyBindApi.get().getKeyCorrelation(key).stream().anyMatch(supplier -> {
            InputBinding binding = supplier.onOrNull(context.controller());
            return binding != null && !binding.isUnbound();
        });
    }
}
