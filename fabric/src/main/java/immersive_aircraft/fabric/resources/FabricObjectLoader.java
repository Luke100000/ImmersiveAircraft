package immersive_aircraft.fabric.resources;

import immersive_aircraft.resources.ObjectLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.util.Identifier;

public class FabricObjectLoader extends ObjectLoader implements IdentifiableResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return ID;
    }
}
