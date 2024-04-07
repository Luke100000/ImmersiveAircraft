package immersive_aircraft.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import immersive_aircraft.Main;
import immersive_aircraft.entity.misc.VehicleData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class VehicleDataLoader extends DataLoader {
    public static final Map<ResourceLocation, VehicleData> REGISTRY = new HashMap<>();
    public static final Map<ResourceLocation, VehicleData> CLIENT_REGISTRY = new HashMap<>();

    private static final VehicleData EMPTY = new VehicleData();

    public VehicleDataLoader() {
        super(new Gson(), "aircraft");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller profiler) {
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

    public static VehicleData get(ResourceLocation identifier) {
        return CLIENT_REGISTRY.getOrDefault(identifier, EMPTY);
    }
}
