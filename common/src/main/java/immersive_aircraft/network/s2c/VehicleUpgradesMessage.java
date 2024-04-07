package immersive_aircraft.network.s2c;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.item.upgrade.VehicleUpgradeRegistry;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.item.upgrade.VehicleUpgrade;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class VehicleUpgradesMessage extends Message {
    private final Map<Item, VehicleUpgrade> upgrades;

    public VehicleUpgradesMessage() {
        this.upgrades = VehicleUpgradeRegistry.INSTANCE.getAll();
    }

    public VehicleUpgradesMessage(FriendlyByteBuf buffer) {
        upgrades = new HashMap<>();

        int upgradeCount = buffer.readInt();
        for (int i = 0; i < upgradeCount; i++) {
            Item item = BuiltInRegistries.ITEM.get(buffer.readResourceLocation());
            upgrades.put(item, readUpgrade(buffer));
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        Map<Item, VehicleUpgrade> upgrades = VehicleUpgradeRegistry.INSTANCE.getAll();
        buffer.writeInt(upgrades.size()); // Write upgrade entry count.

        for (Item item : upgrades.keySet()) {
            buffer.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item));
            writeUpgrade(buffer, upgrades.get(item));
        }
    }

    protected void writeUpgrade(FriendlyByteBuf buffer, VehicleUpgrade upgrade) {
        Map<VehicleStat, Float> upgradeMap = upgrade.getAll();
        buffer.writeInt(upgradeMap.size());
        for (VehicleStat stat : upgradeMap.keySet()) {
            buffer.writeUtf(stat.name());
            buffer.writeFloat(upgradeMap.get(stat));
        }
    }

    protected VehicleUpgrade readUpgrade(FriendlyByteBuf buffer) {
        VehicleUpgrade upgrade = new VehicleUpgrade();
        int statCount = buffer.readInt();
        for (int j = 0; j < statCount; j++) {
            upgrade.set(VehicleStat.STATS.get(buffer.readUtf()), buffer.readFloat());
        }
        return upgrade;
    }

    @Override
    public void receive(Player player) {
        VehicleUpgradeRegistry.INSTANCE.replace(upgrades); // Reset and refill the upgrade registry when the server reloads them.
    }

}
