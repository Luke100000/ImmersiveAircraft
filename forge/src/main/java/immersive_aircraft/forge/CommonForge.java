package immersive_aircraft.forge;

import immersive_aircraft.*;
import immersive_aircraft.forge.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.forge.cobalt.registration.RegistrationImpl;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod(Main.MOD_ID)
@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Bus.MOD)
public final class CommonForge {
    public CommonForge() {
        RegistrationImpl.bootstrap();
        new NetworkHandlerImpl();
        Messages.loadMessages();

        Items.bootstrap();
        Sounds.bootstrap();
        Entities.bootstrap();
    }

    @SubscribeEvent
    public static void register(CreativeModeTabEvent.Register event) {
        ItemGroups.GROUP = event.registerCreativeModeTab(ItemGroups.getIdentifier(), builder -> builder
                .displayName(ItemGroups.getDisplayName())
                .icon(ItemGroups::getIcon)
                .entries((featureFlags, output, hasOp) -> output.addAll(Items.getSortedItems()))
        );
    }
}
