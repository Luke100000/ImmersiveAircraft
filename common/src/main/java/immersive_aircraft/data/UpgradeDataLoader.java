package immersive_aircraft.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import immersive_aircraft.Main;
import immersive_aircraft.item.upgrade.VehicleUpgrade;
import immersive_aircraft.item.upgrade.VehicleUpgradeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

import java.util.Map;

public class UpgradeDataLoader extends DataLoader {
    public UpgradeDataLoader() {
        super(new Gson(), "aircraft_upgrades");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller profiler) {
        // Clear existing upgrade values
        VehicleUpgradeRegistry.INSTANCE.reset();

        jsonMap.forEach((identifier, jsonElement) -> {
            try {
                if (BuiltInRegistries.ITEM.containsKey(identifier)) {
                    Item item = BuiltInRegistries.ITEM.get(identifier);
                    VehicleUpgrade upgrade = getAircraftUpgrade(jsonElement.getAsJsonObject());
                    VehicleUpgradeRegistry.INSTANCE.setUpgrade(item, upgrade);
                } else {
                    Main.LOGGER.error("There is no item {} to make it an upgrade!", identifier);
                }
            } catch (IllegalArgumentException | JsonParseException exception) {
                Main.LOGGER.error("Parsing error on aircraft upgrade {}: {}", identifier, exception.getMessage());
            }
        });
    }
}