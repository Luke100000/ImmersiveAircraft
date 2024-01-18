package immersive_aircraft.item.upgrade;

import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class AircraftUpgradeRegistry {
    public static final AircraftUpgradeRegistry INSTANCE = new AircraftUpgradeRegistry();

    private final Map<Item, AircraftUpgrade> itemUpgrades = new HashMap<>();

    public AircraftUpgrade getUpgrade(Item item) {
        return itemUpgrades.get(item);
    }

    public void setUpgrade(Item item, AircraftUpgrade upgrade) {
        itemUpgrades.put(item, upgrade);
    }

    public void replace(Map<Item, AircraftUpgrade> itemUpgrades) {
        this.itemUpgrades.clear();
        this.itemUpgrades.putAll(itemUpgrades);
    }

    public void reset() {
        itemUpgrades.clear();
    }

    public Map<Item, AircraftUpgrade> getAll() {
        return itemUpgrades;
    }

    public boolean hasUpgrade(Item item) {
        return itemUpgrades.containsKey(item);
    }
}
