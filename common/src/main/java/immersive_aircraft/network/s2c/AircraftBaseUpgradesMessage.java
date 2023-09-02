package immersive_aircraft.network.s2c;

import immersive_aircraft.entity.misc.AircraftBaseUpgradeRegistry;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;

public class AircraftBaseUpgradesMessage extends AircraftUpgradesMessage {

	private final Map<EntityType<?>, AircraftUpgrade> upgrades;

	public AircraftBaseUpgradesMessage() {
		this.upgrades = AircraftBaseUpgradeRegistry.INSTANCE.getAll();
	}

	public AircraftBaseUpgradesMessage(PacketByteBuf buffer) {
		upgrades = new HashMap<>();

		int upgradeCount = buffer.readInt();
		for(int i = 0; i < upgradeCount; i++) {
			EntityType<?> type = Registries.ENTITY_TYPE.get(buffer.readIdentifier());
			upgrades.put(type, readUpgrade(buffer));
		}
	}

	@Override
	public void encode(PacketByteBuf buffer) {
		Map<EntityType<?>, AircraftUpgrade> upgrades = AircraftBaseUpgradeRegistry.INSTANCE.getAll();
		buffer.writeInt(upgrades.size());

		for(EntityType<?> type : upgrades.keySet()) {
			buffer.writeIdentifier(Registries.ENTITY_TYPE.getId(type));
			writeUpgrade(buffer, upgrades.get(type));
		}
	}

	@Override
	public void receive(PlayerEntity player) {
		AircraftBaseUpgradeRegistry.INSTANCE.replace(upgrades); // Reset and refill the upgrade registry when the server reloads them.
	}

}
