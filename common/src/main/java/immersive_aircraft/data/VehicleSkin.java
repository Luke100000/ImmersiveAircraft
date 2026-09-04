package immersive_aircraft.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import immersive_aircraft.entity.misc.BoundingBoxDescriptor;
import immersive_aircraft.entity.misc.PositionDescriptor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * A data-pack-defined cosmetic model for an existing vehicle type.
 *
 * <p>The server owns the catalogue and validates unlock tags. Clients receive
 * the same definitions so renderers can resolve a selected skin to a model.</p>
 */
public record VehicleSkin(
        ResourceLocation id,
        ResourceLocation vehicle,
        ResourceLocation model,
        String translationKey,
        String unlockTag,
        boolean free,
        boolean defaultSkin,
        float scale,
        List<PositionDescriptor> seats,
        List<BoundingBoxDescriptor> boundingBoxes
) {
    public static final String UNLOCK_ALL_TAG = "ia.skin.all";

    public static VehicleSkin fromJson(ResourceLocation id, JsonObject json) {
        ResourceLocation vehicle = new ResourceLocation(GsonHelper.getAsString(json, "vehicle"));
        ResourceLocation model = new ResourceLocation(GsonHelper.getAsString(json, "model"));
        String translationKey = GsonHelper.getAsString(json, "translationKey",
                "vehicle_skin." + id.getNamespace() + "." + id.getPath().replace('/', '.'));
        String unlockTag = GsonHelper.getAsString(json, "unlockTag",
                "ia.skin." + id.getNamespace() + "." + id.getPath().replace('/', '.'));
        boolean free = GsonHelper.getAsBoolean(json, "free", false);
        boolean defaultSkin = GsonHelper.getAsBoolean(json, "default", false);
        float scale = GsonHelper.getAsFloat(json, "scale", 1.0f);
        if (!Float.isFinite(scale) || scale <= 0.0f) {
            throw new IllegalArgumentException("Vehicle skin scale must be a positive finite number");
        }
        List<PositionDescriptor> seats = new ArrayList<>();
        if (json.has("seats")) {
            JsonArray seatArray = GsonHelper.getAsJsonArray(json, "seats");
            seatArray.forEach(element -> seats.add(PositionDescriptor.fromJson(element.getAsJsonObject())));
        }
        List<BoundingBoxDescriptor> boundingBoxes = new ArrayList<>();
        if (json.has("boundingBoxes")) {
            JsonArray boundingBoxArray = GsonHelper.getAsJsonArray(json, "boundingBoxes");
            boundingBoxArray.forEach(element -> boundingBoxes.add(BoundingBoxDescriptor.fromJson(element.getAsJsonObject())));
        }
        return new VehicleSkin(id, vehicle, model, translationKey, unlockTag, free, defaultSkin, scale,
                List.copyOf(seats), List.copyOf(boundingBoxes));
    }

    public VehicleSkin(FriendlyByteBuf buffer) {
        this(
                buffer.readResourceLocation(),
                buffer.readResourceLocation(),
                buffer.readResourceLocation(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readFloat(),
                decodeSeats(buffer),
                decodeBoundingBoxes(buffer)
        );
    }

    private static List<PositionDescriptor> decodeSeats(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<PositionDescriptor> seats = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            seats.add(PositionDescriptor.decode(buffer));
        }
        return List.copyOf(seats);
    }

    private static List<BoundingBoxDescriptor> decodeBoundingBoxes(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<BoundingBoxDescriptor> boundingBoxes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            boundingBoxes.add(BoundingBoxDescriptor.decode(buffer));
        }
        return List.copyOf(boundingBoxes);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(id);
        buffer.writeResourceLocation(vehicle);
        buffer.writeResourceLocation(model);
        buffer.writeUtf(translationKey);
        buffer.writeUtf(unlockTag);
        buffer.writeBoolean(free);
        buffer.writeBoolean(defaultSkin);
        buffer.writeFloat(scale);
        buffer.writeVarInt(seats.size());
        seats.forEach(seat -> seat.encode(buffer));
        buffer.writeVarInt(boundingBoxes.size());
        boundingBoxes.forEach(boundingBox -> boundingBox.encode(buffer));
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    public boolean isUnlockedFor(Player player) {
        return defaultSkin || free || player.getTags().contains(UNLOCK_ALL_TAG) || player.getTags().contains(unlockTag);
    }
}
