package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

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
        return Registration.register(Registries.SOUND_EVENT, id, () -> SoundEvent.of(id));
    }
}
