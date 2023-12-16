package immersive_aircraft.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import immersive_aircraft.Main;
import immersive_aircraft.entity.misc.AircraftBaseUpgradeRegistry;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public class BaseStatDataLoader extends DataLoader {
    public BaseStatDataLoader() {
        super(new Gson(), "aircraft_base_upgrades");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller profiler) {
        // Clear existing upgrade values
        AircraftBaseUpgradeRegistry.INSTANCE.reset();

        jsonMap.forEach((identifier, jsonElement) -> {
            try {
                if (Registry.ENTITY_TYPE.containsKey(identifier)) {
                    EntityType<?> type = Registry.ENTITY_TYPE.get(identifier);
                    AircraftUpgrade upgrade = getAircraftUpgrade(jsonElement.getAsJsonObject());
                    AircraftBaseUpgradeRegistry.INSTANCE.setUpgradeModifier(type, upgrade);
                } else {
                    Main.LOGGER.error("There is no entity {} to apply a base upgrade!", identifier);
                }
            } catch (IllegalArgumentException | JsonParseException exception) {
                Main.LOGGER.error("Parsing error on aircraft base upgrade {}: {}", identifier, exception.getMessage());
            }
        });
    }
}