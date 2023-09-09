package immersive_aircraft.entity.misc;

import immersive_aircraft.item.upgrade.AircraftUpgrade;
import java.util.*;
import net.minecraft.world.entity.EntityType;

public class AircraftBaseUpgradeRegistry {

	public static final AircraftBaseUpgradeRegistry INSTANCE = new AircraftBaseUpgradeRegistry();
	private final Map<EntityType<?>, AircraftUpgrade> upgradeModifiers = new HashMap<>();

	public AircraftBaseUpgradeRegistry() {

	}

	public void setUpgradeModifier(EntityType<?> type, AircraftUpgrade upgrade) {
		upgradeModifiers.put(type, upgrade);
	}

	public AircraftUpgrade getUpgradeModifier(EntityType<?> type) {
		return upgradeModifiers.get(type);
	}

	public void reset() {
		upgradeModifiers.clear();
	}

	public void replace(Map<EntityType<?>, AircraftUpgrade> upgradeModifiers) {
		reset();
		this.upgradeModifiers.putAll(upgradeModifiers);
	}

	public Map<EntityType<?>, AircraftUpgrade> getAll() {
		return upgradeModifiers;
	}

}
