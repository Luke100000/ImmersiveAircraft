package immersive_aircraft.data;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

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
        boolean defaultSkin
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
        return new VehicleSkin(id, vehicle, model, translationKey, unlockTag, free, defaultSkin);
    }

    public VehicleSkin(FriendlyByteBuf buffer) {
        this(
                buffer.readResourceLocation(),
                buffer.readResourceLocation(),
                buffer.readResourceLocation(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readBoolean(),
                buffer.readBoolean()
        );
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(id);
        buffer.writeResourceLocation(vehicle);
        buffer.writeResourceLocation(model);
        buffer.writeUtf(translationKey);
        buffer.writeUtf(unlockTag);
        buffer.writeBoolean(free);
        buffer.writeBoolean(defaultSkin);
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    public boolean isUnlockedFor(Player player) {
        return defaultSkin || free || player.getTags().contains(UNLOCK_ALL_TAG) || player.getTags().contains(unlockTag);
    }
}
