package immersive_aircraft.resources;

import immersive_aircraft.Main;
import immersive_aircraft.util.obj.Builder;
import immersive_aircraft.util.obj.Mesh;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class ObjectLoader extends SimplePreparableReloadListener<Map<ResourceLocation, Resource>> {
    protected static final ResourceLocation ID = Main.locate("objects");

    public final static Map<ResourceLocation, Map<String, Mesh>> objects = new HashMap<>();

    @Override
    protected Map<ResourceLocation, Resource> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return manager.listResources("objects", n -> n.getPath().endsWith(".obj"));
    }

    @Override
    protected void apply(Map<ResourceLocation, Resource> o, ResourceManager manager, ProfilerFiller profiler) {
        objects.clear();
        o.forEach((id, res) -> {
            try {
                InputStream stream = res.open();
                Map<String, Mesh> faces = new Builder(new BufferedReader(new InputStreamReader(stream))).objects;
                ResourceLocation newId = new ResourceLocation(id.getNamespace(), id.getPath());
                objects.put(newId, faces);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
