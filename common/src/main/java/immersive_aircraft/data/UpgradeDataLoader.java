package immersive_aircraft.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import immersive_aircraft.Main;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import immersive_aircraft.item.upgrade.AircraftUpgradeRegistry;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;

public class UpgradeDataLoader extends JsonDataLoader {

	public UpgradeDataLoader() {
		super(new Gson(), "aircraft_upgrades");
	}

	@Override
	protected void apply(Map<Identifier, JsonElement> jsonMap, ResourceManager manager, Profiler profiler) {
		AircraftUpgradeRegistry.INSTANCE.reset(); // Clear existing upgrade values
		jsonMap.forEach((identifier, jsonElement) -> {
			try {
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

			} catch (IllegalArgumentException | JsonParseException exception) {
				Main.LOGGER.error("Parsing error on aircraft upgrade {}: {}", identifier, exception.getMessage());
			}

		});
	}

}