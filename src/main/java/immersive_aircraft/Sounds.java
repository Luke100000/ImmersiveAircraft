package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import net.minecraft.util.SoundEvent;

import java.util.function.Supplier;

public interface Sounds {
    Supplier<SoundEvent> ENGINE_START = register("engine_start");
    Supplier<SoundEvent> PROPELLER = register("propeller");
    Supplier<SoundEvent> PROPELLER_SMALL = register("propeller_small");
    Supplier<SoundEvent> PROPELLER_TINY = register("propeller_tiny");
    Supplier<SoundEvent> WOOSH = register("woosh");
    Supplier<SoundEvent> CANNON = register("cannon");
    Supplier<SoundEvent> WARSHIP = register("warship");
    Supplier<SoundEvent> ENGINE_START_WARSHIP = register("engine_start_warship");
    Supplier<SoundEvent> ENGINE_START_BAMBOO_HOPPER = register("engine_start_bamboo_hopper");
    Supplier<SoundEvent> PROPELLER_BAMBOO_HOPPER = register("propeller_bamboo_hopper");

    static void bootstrap() {

    }

    static Supplier<SoundEvent> register(String name) {
        return Registration.registerSound(name);
    }
}
