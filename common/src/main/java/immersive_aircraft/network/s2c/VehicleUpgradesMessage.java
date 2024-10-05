package immersive_aircraft.network.s2c;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.item.upgrade.VehicleUpgrade;
import immersive_aircraft.item.upgrade.VehicleUpgradeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class VehicleUpgradesMessage extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, VehicleUpgradesMessage> STREAM_CODEC = StreamCodec.ofMember(VehicleUpgradesMessage::encode, VehicleUpgradesMessage::new);
    public static final CustomPacketPayload.Type<VehicleUpgradesMessage> TYPE = Message.createType("vehicle_upgrades");

    public CustomPacketPayload.Type<VehicleUpgradesMessage> type() {
        return TYPE;
    }

    private final Map<Item, VehicleUpgrade> upgrades;

    public VehicleUpgradesMessage() {
        this.upgrades = VehicleUpgradeRegistry.INSTANCE.getAll();
    }

    public VehicleUpgradesMessage(RegistryFriendlyByteBuf buffer) {
        upgrades = new HashMap<>();

        int upgradeCount = buffer.readInt();
        for (int i = 0; i < upgradeCount; i++) {
            Item item = BuiltInRegistries.ITEM.get(buffer.readResourceLocation());
            upgrades.put(item, readUpgrade(buffer));
        }
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        Map<Item, VehicleUpgrade> upgrades = VehicleUpgradeRegistry.INSTANCE.getAll();
        buffer.writeInt(upgrades.size()); // Write upgrade entry count.

        for (Item item : upgrades.keySet()) {
            buffer.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item));
            writeUpgrade(buffer, upgrades.get(item));
        }
    }

    protected void writeUpgrade(RegistryFriendlyByteBuf buffer, VehicleUpgrade upgrade) {
        Map<VehicleStat, Float> upgradeMap = upgrade.getAll();
        buffer.writeInt(upgradeMap.size());
        for (VehicleStat stat : upgradeMap.keySet()) {
            buffer.writeUtf(stat.name());
            buffer.writeFloat(upgradeMap.get(stat));
        }
    }

    protected VehicleUpgrade readUpgrade(RegistryFriendlyByteBuf buffer) {
        VehicleUpgrade upgrade = new VehicleUpgrade();
        int statCount = buffer.readInt();
        for (int j = 0; j < statCount; j++) {
            upgrade.set(VehicleStat.STATS.get(buffer.readUtf()), buffer.readFloat());
        }
        return upgrade;
    }

    @Override
    public void receiveClient() {
        // Reset and refill the upgrade registry when the server reloads them.
        VehicleUpgradeRegistry.INSTANCE.replace(upgrades);
    }
}
