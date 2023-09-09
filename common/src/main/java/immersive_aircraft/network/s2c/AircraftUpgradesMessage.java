package immersive_aircraft.network.s2c;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import immersive_aircraft.item.upgrade.AircraftUpgradeRegistry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class AircraftUpgradesMessage extends Message {

	private final Map<Item, AircraftUpgrade> upgrades;

	public AircraftUpgradesMessage() {
		this.upgrades = AircraftUpgradeRegistry.INSTANCE.getAll();
	}

	public AircraftUpgradesMessage(FriendlyByteBuf buffer) {
		upgrades = new HashMap<>();

		int upgradeCount = buffer.readInt();
		for(int i = 0; i < upgradeCount; i++) {
			Item item = Registry.ITEM.get(buffer.readResourceLocation());
			upgrades.put(item, readUpgrade(buffer));
		}
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		Map<Item, AircraftUpgrade> upgrades = AircraftUpgradeRegistry.INSTANCE.getAll();
		buffer.writeInt(upgrades.size()); // Write upgrade entry count.

		for(Item item : upgrades.keySet()) {
			buffer.writeResourceLocation(Registry.ITEM.getKey(item));
			writeUpgrade(buffer, upgrades.get(item));
		}
	}

	protected void writeUpgrade(FriendlyByteBuf buffer, AircraftUpgrade upgrade) {
		Map<AircraftStat, Float> upgradeMap = upgrade.getAll();
		buffer.writeInt(upgradeMap.size());
		for(AircraftStat stat : upgradeMap.keySet()) {
			buffer.writeInt(stat.ordinal());
			buffer.writeFloat(upgradeMap.get(stat));
		}
	}

	protected AircraftUpgrade readUpgrade(FriendlyByteBuf buffer) {
		AircraftUpgrade upgrade = new AircraftUpgrade();
		int statCount = buffer.readInt();
		for(int j = 0; j < statCount; j++)
			upgrade.set(AircraftStat.values()[buffer.readInt()], buffer.readFloat());
		return upgrade;
	}

	@Override
	public void receive(Player player) {
		AircraftUpgradeRegistry.INSTANCE.replace(upgrades); // Reset and refill the upgrade registry when the server reloads them.
	}

}
