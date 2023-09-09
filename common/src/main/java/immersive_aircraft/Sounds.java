package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public interface Sounds {
    Supplier<SoundEvent> ENGINE_START = register("engine_start");
    Supplier<SoundEvent> PROPELLER = register("propeller");
    Supplier<SoundEvent> PROPELLER_SMALL = register("propeller_small");
    Supplier<SoundEvent> PROPELLER_TINY = register("propeller_tiny");
    Supplier<SoundEvent> WOOSH = register("woosh");

    static void bootstrap() {

    }

    static Supplier<SoundEvent> register(String name) {
        ResourceLocation id = Main.locate(name);
        return Registration.register(Registry.SOUND_EVENT, id, () -> new SoundEvent(id));
    }
}
