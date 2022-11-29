package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public interface Sounds {
    Supplier<SoundEvent> ENGINE_START = register("engine_start");
    Supplier<SoundEvent> PROPELLER = register("propeller");
    Supplier<SoundEvent> PROPELLER_SMALL = register("propeller_small");
    Supplier<SoundEvent> WOOSH = register("woosh");

    static void bootstrap() {

    }

    static Supplier<SoundEvent> register(String name) {
        Identifier id = Main.locate(name);
        return Registration.register(Registry.SOUND_EVENT, id, () -> new SoundEvent(id));
    }
}
