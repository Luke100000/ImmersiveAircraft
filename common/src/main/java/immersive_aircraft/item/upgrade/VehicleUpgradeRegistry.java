package immersive_aircraft.item.upgrade;

import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class VehicleUpgradeRegistry {
    public static final VehicleUpgradeRegistry INSTANCE = new VehicleUpgradeRegistry();

    private final Map<Item, VehicleUpgrade> itemUpgrades = new HashMap<>();

    public VehicleUpgrade getUpgrade(Item item) {
        return itemUpgrades.get(item);
    }

    public void setUpgrade(Item item, VehicleUpgrade upgrade) {
        itemUpgrades.put(item, upgrade);
    }

    public void replace(Map<Item, VehicleUpgrade> itemUpgrades) {
        this.itemUpgrades.clear();
        this.itemUpgrades.putAll(itemUpgrades);
    }

    public void reset() {
        itemUpgrades.clear();
    }

    public Map<Item, VehicleUpgrade> getAll() {
        return itemUpgrades;
    }

    public boolean hasUpgrade(Item item) {
        return itemUpgrades.containsKey(item);
    }
}
