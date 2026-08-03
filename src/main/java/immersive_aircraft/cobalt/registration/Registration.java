package immersive_aircraft.cobalt.registration;

import immersive_aircraft.ItemGroups;
import immersive_aircraft.Main;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 1.12.2 port of the cobalt registration helper. Instead of deferred registry repos,
 * objects are created eagerly during static init, collected here and handed to the
 * game in {@link immersive_aircraft.RegistryHandler} during the registry events.
 */
public class Registration {
    public static final List<Item> ITEMS = new LinkedList<>();
    public static final List<SoundEvent> SOUNDS = new LinkedList<>();

    public static <T extends Item> Supplier<T> register(String name, Supplier<T> obj) {
        final T item = obj.get();
        item.setRegistryName(Main.locate(name));
        item.setTranslationKey(Main.MOD_ID + "." + name);
        item.setCreativeTab(ItemGroups.GROUP);
        ITEMS.add(item);
        return new Supplier<T>() {
            @Override
            public T get() {
                return item;
            }
        };
    }

    public static Supplier<SoundEvent> registerSound(String name) {
        final SoundEvent sound = new SoundEvent(Main.locate(name));
        sound.setRegistryName(Main.locate(name));
        SOUNDS.add(sound);
        return new Supplier<SoundEvent>() {
            @Override
            public SoundEvent get() {
                return sound;
            }
        };
    }
}
