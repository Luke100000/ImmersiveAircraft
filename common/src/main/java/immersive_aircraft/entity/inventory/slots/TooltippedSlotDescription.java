package immersive_aircraft.entity.inventory.slots;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public abstract class TooltippedSlotDescription extends SlotDescription {
    public TooltippedSlotDescription(String type, int index, int x, int y, JsonObject json) {
        super(type, index, x, y, json);
    }

    public TooltippedSlotDescription(String type, FriendlyByteBuf buffer) {
        super(type, buffer);
    }

    public Optional<List<Component>> getToolTip() {
        return Optional.of(List.of(Component.translatable("immersive_aircraft.slot." + type().toLowerCase(Locale.ROOT))));
    }
}
