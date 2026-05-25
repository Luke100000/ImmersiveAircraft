package immersive_aircraft.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import immersive_aircraft.Main;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.item.upgrade.VehicleUpgrade;
import immersive_aircraft.item.upgrade.VehicleUpgradeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpgradeDataLoader extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
    private static final FileToIdConverter LISTER = FileToIdConverter.json("aircraft_upgrades");

    public UpgradeDataLoader() {
    }

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Identifier, JsonElement> map = new HashMap<>();
        for (Map.Entry<Identifier, Resource> entry : LISTER.listMatchingResources(resourceManager).entrySet()) {
            Identifier fileId = entry.getKey();
            Identifier id = LISTER.fileToId(fileId);
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                map.put(id, jsonElement);
            } catch (JsonParseException | IOException | IllegalArgumentException exception) {
                Main.LOGGER.error("Couldn't parse aircraft upgrade data file {} from {}", id, fileId, exception);
            }
        }
        return map;
    }

    @NotNull
    static VehicleUpgrade getAircraftUpgrade(JsonObject jsonObject) {
        VehicleUpgrade upgrade = new VehicleUpgrade();
        for (String key : jsonObject.keySet()) {
            VehicleStat stat = VehicleStat.STATS.get(key);
            if (stat != null) {
                upgrade.set(stat, jsonObject.get(key).getAsFloat());
            }
        }
        return upgrade;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller profiler) {
        // Clear existing upgrade values
        VehicleUpgradeRegistry.INSTANCE.reset();

        jsonMap.forEach((identifier, jsonElement) -> {
            try {
                if (BuiltInRegistries.ITEM.containsKey(identifier)) {
                    Item item = BuiltInRegistries.ITEM.getValue(identifier);
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
