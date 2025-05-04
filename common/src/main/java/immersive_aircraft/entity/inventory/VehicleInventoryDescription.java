package immersive_aircraft.entity.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import immersive_aircraft.entity.inventory.slots.*;
import immersive_aircraft.item.WeaponItem;
import immersive_aircraft.util.Rect2iCommon;
import immersive_aircraft.util.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.FireworkRocketItem;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VehicleInventoryDescription {
    int height = 0;
    int lastIndex = 0;
    int lastSyncIndex = 0;

    final List<Rect2iCommon> rectangles = new LinkedList<>();

    public VehicleInventoryDescription() {

    }

    public VehicleInventoryDescription(RegistryFriendlyByteBuf buffer) {
        int slotCount = buffer.readInt();
        for (int i = 0; i < slotCount; i++) {
            String type = buffer.readUtf();
            addSlot(SLOT_DECODER.get(type).decode(type, buffer));
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
            String type = slot.get("type").getAsString();
            int x = slot.get("x").getAsInt();
            int y = slot.get("y").getAsInt();
            boolean boxed = slot.has("boxed") && slot.getAsJsonPrimitive("boxed").getAsBoolean();

            if (!SLOT_TYPES.containsKey(type)) {
                throw new IllegalArgumentException("Unknown slot type: " + type);
            }

            addSlots(type, x, y, cols, rows, boxed, slot);
        });
        build();
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(slots.size());
        for (SlotDescription slot : slots) {
            buffer.writeUtf(slot.type());
            slot.encode(buffer);
        }

        buffer.writeInt(rectangles.size());
        for (Rect2iCommon rectangle : rectangles) {
            buffer.writeInt(rectangle.getX());
            buffer.writeInt(rectangle.getY());
            buffer.writeInt(rectangle.getWidth());
            buffer.writeInt(rectangle.getHeight());
        }
    }

    public static final Map<String, SlotDescription.SlotDescriptionFactory> SLOT_TYPES = new ConcurrentHashMap<>();
    public static final Map<String, SlotDescription.SlotDescriptionDecoder> SLOT_DECODER = new ConcurrentHashMap<>();

    public static String registerSlotType(String name, SlotDescription.SlotDescriptionFactory slotFactory, SlotDescription.SlotDescriptionDecoder slotDecoder) {
        SLOT_TYPES.put(name, slotFactory);
        SLOT_DECODER.put(name, slotDecoder);
        return name;
    }

    public static final String INVENTORY = registerSlotType("inventory", SlotDescription::new, SlotDescription::new);
    public static final String BOILER = registerSlotType("boiler", FuelSlotDescription::new, FuelSlotDescription::new);
    public static final String WEAPON = registerSlotType("weapon",
            (type, index, x, y, json) -> new TypedSlotDescription(type, index, x, y, json, WeaponItem.class, 1),
            (type, buffer) -> new TypedSlotDescription(type, buffer, WeaponItem.class));
    public static final String UPGRADE = registerSlotType("upgrade", UpgradeSlotDescription::new, UpgradeSlotDescription::new);
    public static final String BOOSTER = registerSlotType("booster",
            (type, index, x, y, json) -> new TypedSlotDescription(type, index, x, y, json, FireworkRocketItem.class, 64),
            (type, buffer) -> new TypedSlotDescription(type, buffer, FireworkRocketItem.class));
    public static final String BANNER = registerSlotType("banner",
            (type, index, x, y, json) -> new TypedSlotDescription(type, index, x, y, json, BannerItem.class, 1),
            (type, buffer) -> new TypedSlotDescription(type, buffer, BannerItem.class));
    public static final String DYE = registerSlotType("dye",
            (type, index, x, y, json) -> new TypedSlotDescription(type, index, x, y, json, DyeItem.class, 1),
            (type, buffer) -> new TypedSlotDescription(type, buffer, DyeItem.class));
    public static final String INGREDIENT = registerSlotType("ingredient", IngredientSlotDescription::new, IngredientSlotDescription::new);


    final HashMap<String, List<SlotDescription>> slotMap = new HashMap<>();
    final List<SlotDescription> slots = new LinkedList<>();

    public int getInventorySize() {
        return slots.size();
    }

    public List<SlotDescription> getSlots() {
        return slots;
    }

    public List<SlotDescription> getSlots(String type) {
        return slotMap.computeIfAbsent(type, k -> new LinkedList<>());
    }

    public VehicleInventoryDescription addSlot(SlotDescription slotDescription) {
        getSlots(slotDescription.type()).add(slotDescription);
        slots.add(slotDescription);

        if (!slotDescription.type().equals("inventory")) {
            lastSyncIndex = slotDescription.index();
        }

        return this;
    }

    public VehicleInventoryDescription addSlots(String type, int x, int y, int cols, int rows, boolean boxed, JsonObject json) {
        if (boxed) {
            addRectangle(x - 8, y - 8, cols * 18 + 14, rows * 18 + 14);
        }
        for (int sx = 0; sx < cols; sx++) {
            for (int sy = 0; sy < rows; sy++) {
                addSlot(SLOT_TYPES.get(type).construct(type, lastIndex++, x + sx * 18, y + sy * 18, json));
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
            if (slot.x() >= 0 && slot.x() < 176) {
                height = Math.max(height, slot.y() + 18);
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
