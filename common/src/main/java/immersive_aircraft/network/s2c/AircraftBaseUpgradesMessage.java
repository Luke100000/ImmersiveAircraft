package immersive_aircraft.network.s2c;

import immersive_aircraft.entity.misc.AircraftBaseUpgradeRegistry;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class AircraftBaseUpgradesMessage extends AircraftUpgradesMessage {

    private final Map<EntityType<?>, AircraftUpgrade> upgrades;

    public AircraftBaseUpgradesMessage() {
        this.upgrades = AircraftBaseUpgradeRegistry.INSTANCE.getAll();
    }

    public AircraftBaseUpgradesMessage(FriendlyByteBuf buffer) {
        upgrades = new HashMap<>();

        int upgradeCount = buffer.readInt();
        for (int i = 0; i < upgradeCount; i++) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(buffer.readResourceLocation());
            upgrades.put(type, readUpgrade(buffer));
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        Map<EntityType<?>, AircraftUpgrade> upgrades = AircraftBaseUpgradeRegistry.INSTANCE.getAll();
        buffer.writeInt(upgrades.size());

        for (EntityType<?> type : upgrades.keySet()) {
            buffer.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(type));
            writeUpgrade(buffer, upgrades.get(type));
        }
    }

    @Override
    public void receive(Player player) {
        AircraftBaseUpgradeRegistry.INSTANCE.replace(upgrades); // Reset and refill the upgrade registry when the server reloads them.
    }

}
