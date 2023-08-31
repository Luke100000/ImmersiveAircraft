package immersive_aircraft.network.s2c;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import immersive_aircraft.item.upgrade.AircraftUpgradeRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;

public class AircraftUpgradeMessage extends Message {

	private final Map<Item, AircraftUpgrade> upgrades;

	public AircraftUpgradeMessage() {
		this.upgrades = AircraftUpgradeRegistry.INSTANCE.getAll();
	}

	public AircraftUpgradeMessage(PacketByteBuf buffer) {
		upgrades = new HashMap<>();

		int upgradeCount = buffer.readInt();
		for(int i = 0; i < upgradeCount; i++) {
			Item item = Registries.ITEM.get(buffer.readIdentifier());
			int statCount = buffer.readInt();

			AircraftUpgrade upgrade = new AircraftUpgrade();
			for(int j = 0; j < statCount; j++)
				upgrade.set(AircraftStat.values()[buffer.readInt()], buffer.readFloat());

			upgrades.put(item, upgrade);
		}
	}

	@Override
	public void encode(PacketByteBuf buffer) {
		Map<Item, AircraftUpgrade> upgrades = AircraftUpgradeRegistry.INSTANCE.getAll();
		buffer.writeInt(upgrades.size()); // Write upgrade entry count.

		for(Item item : upgrades.keySet()) {
			Map<AircraftStat, Float> upgradeMap = AircraftUpgradeRegistry.INSTANCE.getUpgrade(item).getAll();

			buffer.writeIdentifier(Registries.ITEM.getId(item));
			buffer.writeInt(upgradeMap.size());

			for(AircraftStat stat : upgradeMap.keySet()) {
				buffer.writeInt(stat.ordinal());
				buffer.writeFloat(upgradeMap.get(stat));
			}
		}
	}

	@Override
	public void receive(PlayerEntity player) {
		AircraftUpgradeRegistry.INSTANCE.replace(upgrades); // Reset and refill the upgrade registry when the server reloads them.
	}

}
