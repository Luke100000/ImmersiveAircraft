package immersive_aircraft.forge;

import immersive_aircraft.Entities;
import immersive_aircraft.Items;
import immersive_aircraft.Main;
import immersive_aircraft.Messages;
import immersive_aircraft.forge.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.forge.cobalt.registration.RegistrationImpl;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod(Main.MOD_ID)
@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Bus.MOD)
public final class CommonForge {
    public CommonForge() {
        RegistrationImpl.bootstrap();
        new NetworkHandlerImpl();
    }

    @SubscribeEvent
    public static void onRegistryEvent(RegistryEvent<?> event) {
        Items.bootstrap();
        Entities.bootstrap();
        Messages.bootstrap();
    }

    @SubscribeEvent
    public static void onCreateEntityAttributes(EntityAttributeCreationEvent event) {
        RegistrationImpl.ENTITY_ATTRIBUTES.forEach((type, attributes) -> event.put(type, attributes.get().build()));
    }
}
