package immersive_aircraft.entity.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.item.WeaponItem;
import immersive_aircraft.screen.slot.FuelSlot;
import immersive_aircraft.screen.slot.TypedSlot;
import immersive_aircraft.screen.slot.UpgradeSlot;
import immersive_aircraft.util.Rect2iCommon;
import immersive_aircraft.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.FireworkRocketItem;

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
        int rectCount = buffer.readInt();
        for (int i = 0; i < rectCount; i++) {
            addRectangle(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
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
        for (SlotDescription slot : slots) {
            buffer.writeInt(slot.type.ordinal());
            buffer.writeInt(slot.x);
            buffer.writeInt(slot.y);
        }
        buffer.writeInt(rectangles.size());
        for (Rect2iCommon rectangle : rectangles) {
            buffer.writeInt(rectangle.getX());
            buffer.writeInt(rectangle.getY());
            buffer.writeInt(rectangle.getWidth());
            buffer.writeInt(rectangle.getHeight());
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

    public record SlotDescription(int x, int y, int index, SlotType type) {
        public Slot getSlot(InventoryVehicleEntity vehicle, Container inventory, int titleHeight) {
            if (type() == VehicleInventoryDescription.SlotType.BOILER) {
                return new FuelSlot(inventory, index(), x(), y() + titleHeight);
            } else if (type() == VehicleInventoryDescription.SlotType.WEAPON) {
                return new TypedSlot(WeaponItem.class, 1, inventory, index(), x(), y() + titleHeight);
            } else if (type() == VehicleInventoryDescription.SlotType.UPGRADE) {
                return new UpgradeSlot(vehicle, 1, inventory, index(), x(), y() + titleHeight);
            } else if (type() == VehicleInventoryDescription.SlotType.BOOSTER) {
                return new TypedSlot(FireworkRocketItem.class, 64, inventory, index(), x(), y() + titleHeight);
            } else if (type() == VehicleInventoryDescription.SlotType.BANNER) {
                return new TypedSlot(BannerItem.class, 1, inventory, index(), x(), y() + titleHeight);
            } else if (type() == VehicleInventoryDescription.SlotType.DYE) {
                return new TypedSlot(DyeItem.class, 1, inventory, index(), x(), y() + titleHeight);
            } else {
                return new net.minecraft.world.inventory.Slot(inventory, index(), x(), y() + titleHeight);
            }
        }
    }

    final EnumMap<SlotType, List<SlotDescription>> slotMap = new EnumMap<>(SlotType.class);
    final List<SlotDescription> slots = new LinkedList<>();

    {
        for (SlotType value : SlotType.values()) {
            slotMap.put(value, new LinkedList<>());
        }
    }

    public int getInventorySize() {
        return slots.size();
    }

    public List<SlotDescription> getSlots() {
        return slots;
    }

    public List<SlotDescription> getSlots(SlotType type) {
        return slotMap.get(type);
    }

    public VehicleInventoryDescription addSlot(SlotType type, int x, int y) {
        SlotDescription slot = new SlotDescription(x, y, lastIndex++, type);
        slotMap.get(type).add(slot);
        slots.add(slot);

        if (type != SlotType.INVENTORY) {
            lastSyncIndex = lastIndex;
        }

        return this;
    }

    public VehicleInventoryDescription addSlots(SlotType type, int x, int y, int cols, int rows, boolean boxed) {
        if (boxed) {
            addRectangle(x - 8, y + 2, rows * 18 + 14, cols * 18 + 14);
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
        for (SlotDescription slot : slots) {
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
