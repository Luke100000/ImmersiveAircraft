package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public interface Sounds {
    Supplier<SoundEvent> ENGINE_START = register("engine_start");
    Supplier<SoundEvent> ENGINE_START_BAMBOO_HOPPER = register("engine_start_bamboo_hopper");
    Supplier<SoundEvent> ENGINE_START_WARSHIP = register("engine_start_warship");
    Supplier<SoundEvent> WARSHIP = register("warship");
    Supplier<SoundEvent> PROPELLER = register("propeller");
    Supplier<SoundEvent> PROPELLER_BAMBOO_HOPPER = register("propeller_bamboo_hopper");
    Supplier<SoundEvent> PROPELLER_SMALL = register("propeller_small");
    Supplier<SoundEvent> PROPELLER_TINY = register("propeller_tiny");
    Supplier<SoundEvent> WOOSH = register("woosh");
    Supplier<SoundEvent> REPAIR = register("repair");
    Supplier<SoundEvent> CANNON = register("cannon");

    static void bootstrap() {
        // nop
    }

    static Supplier<SoundEvent> register(String name) {
        Identifier id = Main.locate(name);
        return Registration.register(BuiltInRegistries.SOUND_EVENT, id, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
