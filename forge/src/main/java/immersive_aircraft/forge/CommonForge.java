package immersive_aircraft.forge;

import immersive_aircraft.*;
import immersive_aircraft.forge.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.forge.cobalt.registration.CobaltFuelRegistryImpl;
import immersive_aircraft.forge.cobalt.registration.RegistrationImpl;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB;

@Mod(Main.MOD_ID)
public final class CommonForge {
    public CommonForge(FMLJavaModLoadingContext context) {
        BusGroup modEventBus = context.getModBusGroup();
        Main.MOD_LOADER = "forge";
        CompatUtil.setModLoadedChecker(ModList::isLoaded);

        new RegistrationImpl(modEventBus);
        new NetworkHandlerImpl();
        new CobaltFuelRegistryImpl();

        DataLoaders.bootstrap();
        Items.bootstrap();
        Sounds.bootstrap();
        Entities.bootstrap();
        WeaponRegistry.bootstrap();

        Messages.loadMessages();

        RegisterEvent.getBus(modEventBus).addListener(CommonForge::onRegister);
        DEF_REG.register(modEventBus);
    }

    private static void onRegister(RegisterEvent event) {
        event.register(Registries.CUSTOM_STAT, helper -> AircraftStats.bootstrap());
    }

    public static final DeferredRegister<CreativeModeTab> DEF_REG = DeferredRegister.create(CREATIVE_MODE_TAB, Main.MOD_ID);

    @SuppressWarnings("unused")
    public static final RegistryObject<CreativeModeTab> TAB = DEF_REG.register(Main.MOD_ID, () -> CreativeModeTab.builder()
            .title(ItemGroups.getDisplayName())
            .icon(ItemGroups::getIcon)
            .displayItems((featureFlags, output) -> output.acceptAll(Items.getSortedItems()))
            .build()
    );
}
