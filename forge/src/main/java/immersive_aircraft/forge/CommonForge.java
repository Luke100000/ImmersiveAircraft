package immersive_aircraft.forge;

import immersive_aircraft.*;
import immersive_aircraft.data.UpgradeDataLoader;
import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.forge.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.forge.cobalt.registration.RegistrationImpl;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.RegisterEvent;

@Mod(Main.MOD_ID)
@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Bus.MOD)
public final class CommonForge {
    public CommonForge() {
        RegistrationImpl.bootstrap();
        new NetworkHandlerImpl();
        Messages.loadMessages();
    }

    @SubscribeEvent
    public static void onRegistryEvent(RegisterEvent event) {
        Items.bootstrap();
        Sounds.bootstrap();
        Entities.bootstrap();

        DEF_REG.register(FMLJavaModLoadingContext.get().getModEventBus());
        Registration.registerDataLoader("aircraft_upgrades", new UpgradeDataLoader());
    }
}
