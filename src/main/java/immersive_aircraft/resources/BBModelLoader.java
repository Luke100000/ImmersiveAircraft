package immersive_aircraft.resources;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import immersive_aircraft.Main;
import immersive_aircraft.resources.bbmodel.BBModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 1.12.2 port of the 1.20.1 BBModelLoader. Like {@link ObjectLoader}, models are loaded
 * lazily on first request and the cache is dropped on resource reload (the reload listener
 * is shared with ObjectLoader - see {@link ObjectLoader#onResourceManagerReload}).
 */
public class BBModelLoader {
    public static final Map<ResourceLocation, BBModel> MODELS = new HashMap<>();
    private static final Gson GSON = new Gson();

    public static BBModel getModel(ResourceLocation id) {
        BBModel model = MODELS.get(id);
        if (model == null) {
            model = load(id);
            MODELS.put(id, model);
        }
        return model;
    }

    private static BBModel load(ResourceLocation id) {
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        try {
            IResource resource = manager.getResource(id);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            try {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                return new BBModel(json, id);
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            Main.LOGGER.error("Couldn't parse bbmodel file {}", id, e);
            return null;
        }
    }
}
