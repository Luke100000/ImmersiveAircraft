package immersive_aircraft.resources;

import immersive_aircraft.Main;
import immersive_aircraft.util.obj.Builder;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ObjectLoader extends SinglePreparationResourceReloader<Map<Identifier, Resource>> {
    protected static final Identifier ID = Main.locate("objects");

    public final static Map<Identifier, Map<String, Mesh>> objects = new HashMap<>();

    @Override
    protected Map<Identifier, Resource> prepare(ResourceManager manager, Profiler profiler) {
        return manager.findResources("objects", n -> n.getPath().endsWith(".obj"));
    }

    @Override
    protected void apply(Map<Identifier, Resource> o, ResourceManager manager, Profiler profiler) {
        objects.clear();
        o.forEach((id, res) -> {
            try {
                InputStream stream = res.getInputStream();
                Map<String, Mesh> faces = new Builder(new BufferedReader(new InputStreamReader(stream))).objects;
                Identifier newId = new Identifier(id.getNamespace(), id.getPath());
                objects.put(newId, faces);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
