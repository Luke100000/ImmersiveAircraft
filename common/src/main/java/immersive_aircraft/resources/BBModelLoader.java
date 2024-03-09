package immersive_aircraft.resources;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import immersive_aircraft.Main;
import immersive_aircraft.resources.bbmodel.BBModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class BBModelLoader extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
    protected static final int PATH_SUFFIX_LENGTH = 8;
    protected static final int PATH_PREFIX_LENGTH = 8;

    public static final Map<ResourceLocation, BBModel> MODELS = new HashMap<>();
    private final Gson gson;

    public BBModelLoader() {
        gson = new Gson();
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        HashMap<ResourceLocation, JsonElement> map = Maps.newHashMap();
        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources("objects", n -> n.getPath().endsWith(".bbmodel")).entrySet()) {
            ResourceLocation location = entry.getKey();
            String name = location.getPath();
            ResourceLocation id = new ResourceLocation(location.getNamespace(), name.substring(PATH_PREFIX_LENGTH, name.length() - PATH_SUFFIX_LENGTH));
            try {
                BufferedReader reader = entry.getValue().openAsReader();
                try {
                    JsonElement jsonElement = GsonHelper.fromJson(this.gson, reader, JsonElement.class);
                    map.put(id, jsonElement);
                } finally {
                    ((Reader) reader).close();
                }
            } catch (JsonParseException | IOException | IllegalArgumentException exception) {
                Main.LOGGER.error("Couldn't parse data file {} from {}", id, location, exception);
            }
        }
        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        MODELS.clear();
        jsonMap.forEach((identifier, jsonElement) -> MODELS.put(identifier, new BBModel(jsonElement.getAsJsonObject(), identifier)));
    }
}
