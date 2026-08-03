package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Main.MOD_ID)
public class RegistryHandler {
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        Items.bootstrap();
        for (Item item : Registration.ITEMS) {
            event.getRegistry().register(item);
        }
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        Sounds.bootstrap();
        for (SoundEvent sound : Registration.SOUNDS) {
            event.getRegistry().register(sound);
        }
    }
}
