package immersive_aircraft.forge;

import immersive_aircraft.*;
import immersive_aircraft.forge.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.forge.cobalt.registration.CobaltFuelRegistryImpl;
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
        DataLoaders.bootstrap();

        new NetworkHandlerImpl();
        new CobaltFuelRegistryImpl();

        Messages.loadMessages();
    }

    private static boolean registered = false;

    @SubscribeEvent
    public static void onRegistryEvent(RegisterEvent event) {
        if (!registered) {
            registered = true;

            Items.bootstrap();
            Sounds.bootstrap();
            Entities.bootstrap();
            WeaponRegistry.bootstrap();
        }
    }
}
