package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.data.AircraftDataLoader;
import immersive_aircraft.data.UpgradeDataLoader;
import immersive_aircraft.resources.BBModelLoader;

public class DataLoaders {
    public static void bootstrap() {
        // nop
    }

    static {
        Registration.registerDataLoader("aircraft_upgrades", new UpgradeDataLoader());
        Registration.registerDataLoader("aircraft", new AircraftDataLoader());

        Registration.registerResourceLoader("objects_bbmodel", new BBModelLoader());
    }
}
