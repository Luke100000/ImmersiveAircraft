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
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

public class BaseStatDataLoader extends SimpleJsonResourceReloadListener {

	public BaseStatDataLoader() {
		super(new Gson(), "aircraft_base_upgrades");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller profiler) {
		AircraftBaseUpgradeRegistry.INSTANCE.reset(); // Clear existing upgrade values
		jsonMap.forEach((identifier, jsonElement) -> {
			try {
				JsonObject jsonObject = jsonElement.getAsJsonObject();

				if(Registry.ENTITY_TYPE.containsKey(identifier)) {
					EntityType<?> type = Registry.ENTITY_TYPE.get(identifier); // Grab item used as upgrade.

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