package immersive_aircraft;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.data.BaseStatDataLoader;
import immersive_aircraft.data.UpgradeDataLoader;
import immersive_aircraft.resources.BBModelLoader;
import immersive_aircraft.resources.ObjectLoader;

public interface DataLoaders {
    static void register() {
        Registration.registerDataLoader("aircraft_upgrades", new UpgradeDataLoader());
        Registration.registerDataLoader("aircraft_stats", new BaseStatDataLoader());

        Registration.registerResourceLoader("objects_obj", new ObjectLoader());
        Registration.registerResourceLoader("objects_bbmodel", new BBModelLoader());
    }
}
