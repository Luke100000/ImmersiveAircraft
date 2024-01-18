package immersive_aircraft.entity.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import immersive_aircraft.util.Rect2iCommon;
import immersive_aircraft.util.Utils;
import net.minecraft.network.FriendlyByteBuf;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class VehicleInventoryDescription {
    int height = 0;
    int lastIndex = 0;
    int lastSyncIndex = 0;

    final List<Rect2iCommon> rectangles = new LinkedList<>();

    public VehicleInventoryDescription() {

    }

    public VehicleInventoryDescription(FriendlyByteBuf buffer) {
        int slotCount = buffer.readInt();
        for (int i = 0; i < slotCount; i++) {
            SlotType type = SlotType.values()[buffer.readInt()];
            addSlot(type, buffer.readInt(), buffer.readInt());
        }
        build();
    }

    public VehicleInventoryDescription(JsonArray inventorySlots) {
        inventorySlots.forEach(jsonElement -> {
            JsonObject slot = jsonElement.getAsJsonObject();
            int cols = Utils.getIntElement(slot, "cols", 1);
            int rows = Utils.getIntElement(slot, "rows", 1);
            addSlots(
                    VehicleInventoryDescription.SlotType.valueOf(slot.get("type").getAsString().toUpperCase(Locale.ROOT)),
                    slot.get("x").getAsInt(),
                    slot.get("y").getAsInt(),
                    cols, rows,
                    slot.has("boxed") && slot.getAsJsonPrimitive("boxed").getAsBoolean()
            );
        });
        build();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(slots.size());
        for (Slot slot : slots) {
            buffer.writeInt(slot.type.ordinal());
            buffer.writeInt(slot.x);
            buffer.writeInt(slot.y);
        }
    }

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

    EnumMap<SlotType, List<Slot>> slotMap = new EnumMap<>(SlotType.class);
    final List<Slot> slots = new LinkedList<>();

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

    public VehicleInventoryDescription addSlots(SlotType type, int x, int y, int cols, int rows, boolean boxed) {
        if (boxed) {
            addRectangle(x - 8, y + 2, cols * 18 + 14, rows * 18 + 14);
        }
        for (int sx = 0; sx < cols; sx++) {
            for (int sy = 0; sy < rows; sy++) {
                addSlot(type, x + sx * 18, y + sy * 18);
            }
        }
        return this;
    }

    public VehicleInventoryDescription addRectangle(int x, int y, int w, int h) {
        rectangles.add(new Rect2iCommon(x, y, w, h));
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

    public List<Rect2iCommon> getRectangles() {
        return rectangles;
    }
}
