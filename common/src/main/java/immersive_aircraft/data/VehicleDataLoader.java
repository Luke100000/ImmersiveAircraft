package immersive_aircraft.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import immersive_aircraft.Main;
import immersive_aircraft.entity.misc.VehicleData;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VehicleDataLoader extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
    public static final Map<Identifier, VehicleData> REGISTRY = new HashMap<>();
    public static final Map<Identifier, VehicleData> CLIENT_REGISTRY = new HashMap<>();

    private static final VehicleData EMPTY = new VehicleData();
    private static final FileToIdConverter LISTER = FileToIdConverter.json("aircraft");

    public VehicleDataLoader() {
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
                Main.LOGGER.error("Couldn't parse aircraft data file {} from {}", id, fileId, exception);
            }
        }
        return map;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller profiler) {
        REGISTRY.clear();

        jsonMap.forEach((identifier, jsonElement) -> {
            try {
                VehicleData data = new VehicleData(jsonElement.getAsJsonObject());
                REGISTRY.put(identifier, data);
            } catch (IllegalArgumentException | JsonParseException exception) {
                Main.LOGGER.error("Parsing error on aircraft {}: {}", identifier, exception.getMessage());
            }
        });


        CLIENT_REGISTRY.clear();
        CLIENT_REGISTRY.putAll(REGISTRY);
    }

    public static VehicleData get(Identifier identifier) {
        return CLIENT_REGISTRY.getOrDefault(identifier, EMPTY);
    }
}
