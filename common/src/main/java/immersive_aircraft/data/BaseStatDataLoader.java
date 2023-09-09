package immersive_aircraft.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import immersive_aircraft.Main;
import immersive_aircraft.entity.misc.AircraftBaseUpgradeRegistry;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import immersive_aircraft.item.upgrade.AircraftUpgradeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;

public class BaseStatDataLoader extends JsonDataLoader {

	public BaseStatDataLoader() {
		super(new Gson(), "aircraft_base_upgrades");
	}

	@Override
	protected void apply(Map<Identifier, JsonElement> jsonMap, ResourceManager manager, Profiler profiler) {
		AircraftBaseUpgradeRegistry.INSTANCE.reset(); // Clear existing upgrade values
		jsonMap.forEach((identifier, jsonElement) -> {
			try {
				JsonObject jsonObject = jsonElement.getAsJsonObject();

				if(Registries.ENTITY_TYPE.containsId(identifier)) {
					EntityType<?> type = Registries.ENTITY_TYPE.get(identifier); // Grab item used as upgrade.

					AircraftUpgrade upgrade = new AircraftUpgrade(); // Set up upgrade object.
					for(String key : jsonObject.keySet()) {
						AircraftStat stat = AircraftUpgradeRegistry.STATS.get(key);
						if(stat != null)
							upgrade.set(stat, jsonObject.get(key).getAsFloat());
					}
					AircraftBaseUpgradeRegistry.INSTANCE.setUpgradeModifier(type, upgrade);
				}

			} catch (IllegalArgumentException | JsonParseException exception) {
				Main.LOGGER.error("Parsing error on aircraft base upgrade {}: {}", identifier, exception.getMessage());
			}

		});
	}

}