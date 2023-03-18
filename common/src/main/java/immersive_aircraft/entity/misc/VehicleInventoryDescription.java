package immersive_aircraft.entity.misc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VehicleInventoryDescription {
    int height = 0;
    int storageHeight = 0;
    int lastIndex = 0;
    int lastSyncIndex = 0;

    public enum SlotType {
        INVENTORY,
        STORAGE,
        BOILER,
        WEAPON,
        UPGRADE,
        BOOSTER,
        BANNER,
        DYE
    }

    public static class Slot {
        public final int x, y;
        public final int index;
        public final SlotType type;

        public Slot(int x, int y, int index, SlotType type) {
            this.x = x;
            this.y = y;
            this.index = index;
            this.type = type;
        }
    }

    Map<SlotType, List<Slot>> slotMap = new HashMap<>();
    List<Slot> slots = new LinkedList<>();

    {
        for (SlotType value : SlotType.values()) {
            slotMap.put(value, new LinkedList<>());
        }
    }

    public int getInventorySize() {
        return slots.size();
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public List<Slot> getSlots(SlotType type) {
        return slotMap.get(type);
    }

    public VehicleInventoryDescription addSlot(SlotType type, int x, int y) {
        Slot slot = new Slot(x, y, lastIndex++, type);
        slotMap.get(type).add(slot);
        slots.add(slot);

        if (type != SlotType.INVENTORY && type != SlotType.STORAGE) {
            lastSyncIndex = lastIndex;
        }

        return this;
    }

    public VehicleInventoryDescription addSlots(SlotType type, int x, int y, int cols, int rows) {
        for (int sx = 0; sx < cols; sx++) {
            for (int sy = 0; sy < rows; sy++) {
                addSlot(type, x + sx * 18, y + sy * 18);
            }
        }
        return this;
    }

    public VehicleInventoryDescription build() {
        for (Slot slot : slots) {
            if (slot.type == SlotType.STORAGE) {
                storageHeight = Math.max(storageHeight, slot.y);
            } else {
                height = Math.max(height, slot.y + 28);
            }
        }
        return this;
    }

    public int getHeight() {
        return height;
    }

    public int getStorageHeight() {
        return storageHeight;
    }

    public int getLastSyncIndex() {
        return lastSyncIndex;
    }
}
