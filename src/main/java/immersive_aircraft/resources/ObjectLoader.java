package immersive_aircraft.resources;

import immersive_aircraft.Main;
import immersive_aircraft.util.obj.Builder;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 1.12.2 port note: 1.16 used SinglePreparationResourceReloader + ResourceManager.findResources.
 * 1.12.2 has no resource listing API, so meshes are loaded lazily on first request via
 * {@link #getObject(ResourceLocation, String)} and dropped on resource reload.
 */
public class ObjectLoader implements IResourceManagerReloadListener {
    public static final ObjectLoader INSTANCE = new ObjectLoader();

    public final static Map<ResourceLocation, Map<String, Mesh>> objects = new HashMap<>();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        objects.clear();
        BBModelLoader.MODELS.clear();
        BBModelBridge.clearCache();
    }

    public static Mesh getObject(ResourceLocation id, String object) {
        // Blockbench models are converted on the fly by the bridge
        if (id.getPath().endsWith(".bbmodel")) {
            return BBModelBridge.getMesh(id, object);
        }
        Map<String, Mesh> faces = objects.get(id);
        if (faces == null) {
            faces = load(id);
        }
        return faces.get(object);
    }

    private static Map<String, Mesh> load(ResourceLocation id) {
        try {
            IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
            IResource resource = manager.getResource(id);
            InputStream stream = resource.getInputStream();
            Map<String, Mesh> faces = new Builder(new BufferedReader(new InputStreamReader(stream))).objects;
            objects.put(id, faces);
            return faces;
        } catch (IOException e) {
            Main.LOGGER.error("Failed to load object {}", id, e);
            Map<String, Mesh> empty = new HashMap<>();
            objects.put(id, empty);
            return empty;
        }
    }
}
