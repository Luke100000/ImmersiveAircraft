package immersive_aircraft.fabric.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import immersive_aircraft.Main;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import immersive_aircraft.item.upgrade.AircraftUpgradeRegistry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.Reader;
import java.util.Map.Entry;

public class UpgradeResourceListener implements SimpleSynchronousResourceReloadListener {

	private static final Gson GSON = new Gson();
	private static final String DIRECTORY = "aircraft_upgrades";

	public UpgradeResourceListener() {
		super();
	}

	@Override
	public void reload(ResourceManager manager) {
		AircraftUpgradeRegistry.INSTANCE.reset(); // Clear existing upgrade values
		ResourceFinder finder = ResourceFinder.json(DIRECTORY);

		for(Entry<Identifier, Resource> resource : finder.findResources(manager).entrySet()) {
			try(Reader reader = resource.getValue().getReader()) {
				JsonElement jsonElement = JsonHelper.deserialize(GSON, reader, JsonElement.class);
				Identifier identifier = finder.toResourceId(resource.getKey());

				JsonObject jsonObject = jsonElement.getAsJsonObject();

				if(Registries.ITEM.containsId(identifier)) {
					Item item = Registries.ITEM.get(identifier); // Grab item used as upgrade.

					AircraftUpgrade upgrade = new AircraftUpgrade(); // Set up upgrade object.
					for(String key : jsonObject.keySet()) {
						AircraftStat stat = AircraftUpgradeRegistry.STATS.get(key);
						if(stat != null)
							upgrade.set(stat, jsonObject.get(key).getAsFloat());
					}
					AircraftUpgradeRegistry.INSTANCE.setUpgrade(item, upgrade);
				}

			} catch(Exception e) {
				Main.LOGGER.error("Parsing error on aircraft upgrade {}: {}", resource.getKey(), e.getMessage());
			}

		}
	}

	@Override
	public Identifier getFabricId() {
		return new Identifier(Main.MOD_ID, "aircraft_upgrades");
	}


}