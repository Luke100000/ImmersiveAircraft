package immersive_aircraft.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import immersive_aircraft.Main;
import immersive_aircraft.entity.misc.AircraftData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class AircraftDataLoader extends DataLoader {
    public static final Map<ResourceLocation, AircraftData> REGISTRY = new HashMap<>();
    public static final Map<ResourceLocation, AircraftData> CLIENT_REGISTRY = new HashMap<>();

    private static final AircraftData EMPTY = new AircraftData();

    public AircraftDataLoader() {
        super(new Gson(), "aircraft");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller profiler) {
        REGISTRY.clear();

        jsonMap.forEach((identifier, jsonElement) -> {
            try {
                AircraftData data = new AircraftData(jsonElement.getAsJsonObject());
                REGISTRY.put(identifier, data);
            } catch (IllegalArgumentException | JsonParseException exception) {
                Main.LOGGER.error("Parsing error on aircraft {}: {}", identifier, exception.getMessage());
            }
        });


        CLIENT_REGISTRY.clear();
        CLIENT_REGISTRY.putAll(REGISTRY);
    }

    public static AircraftData get(ResourceLocation identifier) {
        return CLIENT_REGISTRY.getOrDefault(identifier, EMPTY);
    }
}
