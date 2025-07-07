package immersive_aircraft.entity.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import immersive_aircraft.entity.inventory.VehicleInventoryDescription;
import immersive_aircraft.entity.inventory.slots.SlotDescription;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.util.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.*;

public class VehicleData {
    private final Map<VehicleStat, Float> properties = new HashMap<>();
    private final VehicleInventoryDescription inventoryDescription;
    private final Map<Integer, Map<WeaponMount.Type, List<WeaponMount>>> weaponMounts = new HashMap<>();
    private final List<List<PositionDescriptor>> passengerPositions = new LinkedList<>();
    private final List<BoundingBoxDescriptor> boundingBoxes = new LinkedList<>();
    private final List<TrailDescriptor> trails = new LinkedList<>();

    public VehicleData() {
        inventoryDescription = new VehicleInventoryDescription();
    }

    public VehicleData(JsonObject json) {
        // Load properties
        JsonObject propertyJson = json.getAsJsonObject("properties");
        VehicleStat.STATS.values().forEach(stat -> properties.put(stat, Utils.getFloatElement(propertyJson, stat.name(), stat.defaultValue())));

        // Load inventory slots
        inventoryDescription = new VehicleInventoryDescription(json.getAsJsonArray("inventorySlots"));

        // Populate weapon mounts
        populateWeaponMounts();

        // Load weapon mounts
        ArrayList<SlotDescription> weaponSlots = new java.util.ArrayList<>(inventoryDescription.getSlots(VehicleInventoryDescription.WEAPON));
        json.getAsJsonArray("weaponMounts").forEach(weaponMountsJson -> {
            //noinspection SequencedCollectionMethodCanBeUsed
            SlotDescription slot = weaponSlots.remove(0);
            weaponMountsJson.getAsJsonObject().entrySet().forEach(entry -> {
                WeaponMount.Type type = WeaponMount.Type.valueOf(entry.getKey());
                JsonArray mounts = entry.getValue().getAsJsonArray();

                mounts.forEach(mountElement -> {
                    JsonObject mount = mountElement.getAsJsonObject();
                    PositionDescriptor position = PositionDescriptor.fromJson(mount);
                    boolean blocking = mount.has("blocking") && mount.get("blocking").getAsBoolean();
                    weaponMounts.get(slot.index()).get(type).add(new WeaponMount(position.matrix(), blocking));
                });
            });
        });

        // Load passenger positions
        json.getAsJsonArray("passengerPositions").forEach(passengerPositionsSlotJson -> {
            LinkedList<PositionDescriptor> positions = new LinkedList<>();
            passengerPositions.add(positions);
            passengerPositionsSlotJson.getAsJsonArray().forEach(passengerPositionJson -> {
                PositionDescriptor position = PositionDescriptor.fromJson(passengerPositionJson.getAsJsonObject());
                positions.add((position));
            });
        });

        // Load bounding boxes
        json.getAsJsonArray("boundingBoxes").forEach(e -> boundingBoxes.add(BoundingBoxDescriptor.fromJson(e.getAsJsonObject())));

        // Load trails
        JsonArray trailJson = json.getAsJsonArray("trails");
        if (trailJson != null) {
            trailJson.forEach(e -> trails.add(TrailDescriptor.fromJson(e.getAsJsonObject())));
        }
    }

    public VehicleData(RegistryFriendlyByteBuf byteBuf) {
        // Load properties
        int propertiesCount = byteBuf.readInt();
        for (int i = 0; i < propertiesCount; i++) {
            VehicleStat stat = VehicleStat.STATS.get(byteBuf.readUtf());
            properties.put(stat, byteBuf.readFloat());
        }

        // Load inventory slots
        inventoryDescription = new VehicleInventoryDescription(byteBuf);

        // Populate weapon mounts
        populateWeaponMounts();

        // Load weapon mounts
        int weaponMountsCount = byteBuf.readInt();
        for (int i = 0; i < weaponMountsCount; i++) {
            int slot = byteBuf.readInt();
            int typeCount = byteBuf.readInt();
            for (int j = 0; j < typeCount; j++) {
                WeaponMount.Type type = WeaponMount.Type.values()[byteBuf.readInt()];
                int mountCount = byteBuf.readInt();
                for (int k = 0; k < mountCount; k++) {
                    weaponMounts.get(slot).get(type).add(WeaponMount.decode(byteBuf));
                }
            }
        }

        // Load passenger positions
        int passengerPositionsCount = byteBuf.readInt();
        for (int i = 0; i < passengerPositionsCount; i++) {
            int positionsCount = byteBuf.readInt();
            LinkedList<PositionDescriptor> positions = new LinkedList<>();
            passengerPositions.add(positions);
            for (int j = 0; j < positionsCount; j++) {
                positions.add(PositionDescriptor.decode(byteBuf));
            }
        }

        // Load bounding boxes
        int boundingBoxesCount = byteBuf.readInt();
        for (int i = 0; i < boundingBoxesCount; i++) {
            boundingBoxes.add(BoundingBoxDescriptor.decode(byteBuf));
        }

        // Load trails
        int trailsCount = byteBuf.readInt();
        for (int i = 0; i < trailsCount; i++) {
            trails.add(TrailDescriptor.decode(byteBuf));
        }
    }

    private void populateWeaponMounts() {
        List<SlotDescription> weaponSlots = inventoryDescription.getSlots(VehicleInventoryDescription.WEAPON);
        weaponSlots.forEach(slot -> {
            for (WeaponMount.Type type : WeaponMount.Type.values()) {
                weaponMounts.computeIfAbsent(slot.index(), integer -> new HashMap<>()).put(type, new LinkedList<>());
            }
        });
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        // Encode properties
        buffer.writeInt(properties.size());
        properties.forEach((stat, value) -> {
            buffer.writeUtf(stat.name());
            buffer.writeFloat(value);
        });

        inventoryDescription.encode(buffer);

        // Encode weapon mounts
        buffer.writeInt(weaponMounts.size());
        weaponMounts.forEach((slot, mounts) -> {
            buffer.writeInt(slot);
            buffer.writeInt(mounts.size());
            mounts.forEach((type, mountList) -> {
                buffer.writeInt(type.ordinal());
                buffer.writeInt(mountList.size());
                mountList.forEach(mount -> mount.encode(buffer));
            });
        });

        // Encode passenger positions
        buffer.writeInt(passengerPositions.size());
        passengerPositions.forEach(positions -> {
            buffer.writeInt(positions.size());
            positions.forEach(position -> position.encode(buffer));
        });

        // Encode bounding boxes
        buffer.writeInt(boundingBoxes.size());
        boundingBoxes.forEach(boundingBox -> boundingBox.encode(buffer));

        // Encode trails
        buffer.writeInt(trails.size());
        trails.forEach(trail -> trail.encode(buffer));
    }

    public Map<VehicleStat, Float> getProperties() {
        return properties;
    }

    public VehicleInventoryDescription getInventoryDescription() {
        return inventoryDescription;
    }

    public Map<Integer, Map<WeaponMount.Type, List<WeaponMount>>> getWeaponMounts() {
        return weaponMounts;
    }

    public List<BoundingBoxDescriptor> getBoundingBoxes() {
        return boundingBoxes;
    }

    public List<List<PositionDescriptor>> getPassengerPositions() {
        return passengerPositions;
    }

    public List<TrailDescriptor> getTrails() {
        return trails;
    }
}
