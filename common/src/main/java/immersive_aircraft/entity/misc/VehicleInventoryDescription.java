package immersive_aircraft.entity.misc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import record;

public class VehicleInventoryDescription {
    int height = 0;
    int lastIndex = 0;
    int lastSyncIndex = 0;

    public record Rectangle(int x, int y, int w, int h) {
    }

    List<Rectangle> rectangles = new LinkedList<>();

    public enum SlotType {
        INVENTORY,
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

        if (type != SlotType.INVENTORY) {
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

    public VehicleInventoryDescription addBoxedSlots(SlotType type, int x, int y, int cols, int rows) {
        addSlots(type, x, y, cols, rows);
        return addRectangle(x - 8, y + 2, cols * 18 + 14, rows * 18 + 14);
    }

    public VehicleInventoryDescription addRectangle(int x, int y, int w, int h) {
        rectangles.add(new Rectangle(x, y, w, h));
        return this;
    }

    public VehicleInventoryDescription build() {
        for (Slot slot : slots) {
            if (slot.x >= 0 && slot.x < 176) {
                height = Math.max(height, slot.y + 28);
            }
        }
        return this;
    }

    public int getHeight() {
        return height;
    }

    public int getLastSyncIndex() {
        return lastSyncIndex;
    }

    public List<Rectangle> getRectangles() {
        return rectangles;
    }
}
