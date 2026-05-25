package immersive_aircraft.neoforge;

import immersive_aircraft.*;
import immersive_aircraft.neoforge.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.neoforge.cobalt.registration.CobaltFuelRegistryImpl;
import immersive_aircraft.neoforge.cobalt.registration.RegistrationImpl;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import static net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB;

@Mod(Main.MOD_ID)
@EventBusSubscriber(modid = Main.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class CommonNeoForge {
    static {
        Main.MOD_LOADER = "neoforge";

        new CobaltFuelRegistryImpl();
    }

    static final NetworkHandlerImpl NETWORK_HANDLER = new NetworkHandlerImpl();

    public CommonNeoForge(IEventBus bus) {
        new RegistrationImpl(bus);

        DataLoaders.bootstrap();
        Items.bootstrap();
        Sounds.bootstrap();
        Entities.bootstrap();
        WeaponRegistry.bootstrap();

        Messages.loadMessages();

        DEF_REG.register(bus);
    }

    public static final DeferredRegister<CreativeModeTab> DEF_REG = DeferredRegister.create(CREATIVE_MODE_TAB, Main.MOD_ID);

    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = DEF_REG.register(Main.MOD_ID, () -> CreativeModeTab.builder()
            .title(ItemGroups.getDisplayName())
            .icon(ItemGroups::getIcon)
            .displayItems((featureFlags, output) -> output.acceptAll(Items.getSortedItems()))
            .build()
    );

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        CommonNeoForge.NETWORK_HANDLER.register(event);
    }

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        event.register(Registries.CUSTOM_STAT, helper -> AircraftStats.bootstrap());
    }
}
