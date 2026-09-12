package immersive_aircraft.neoforge;

import immersive_aircraft.*;
import immersive_aircraft.neoforge.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.neoforge.cobalt.registration.CobaltFuelRegistryImpl;
import immersive_aircraft.neoforge.cobalt.registration.RegistrationImpl;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Main.MOD_ID)
public final class CommonNeoForge {
    public CommonNeoForge(IEventBus modEventBus) {
        Main.MOD_LOADER = "neoforge";
        CompatUtil.setModLoadedChecker(ModList.get()::isLoaded);

        new RegistrationImpl(modEventBus);
        NetworkHandlerImpl networkHandler = new NetworkHandlerImpl(modEventBus);
        new CobaltFuelRegistryImpl();
        NeoForgeBusEvents.register();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            ClientNeoForge.register(modEventBus, networkHandler);
        }

        DataLoaders.bootstrap();
        Items.bootstrap();
        Sounds.bootstrap();
        Entities.bootstrap();
        WeaponRegistry.bootstrap();

        Messages.loadMessages();

        modEventBus.addListener(CommonNeoForge::onRegister);
        DEF_REG.register(modEventBus);
    }

    private static void onRegister(RegisterEvent event) {
        event.register(Registries.CUSTOM_STAT, helper -> AircraftStats.bootstrap());
    }

    public static final DeferredRegister<CreativeModeTab> DEF_REG = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Main.MOD_ID);

    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = DEF_REG.register(Main.MOD_ID, () -> CreativeModeTab.builder()
            .title(ItemGroups.getDisplayName())
            .icon(ItemGroups::getIcon)
            .displayItems((featureFlags, output) -> output.acceptAll(Items.getSortedItems()))
            .build()
    );
}
