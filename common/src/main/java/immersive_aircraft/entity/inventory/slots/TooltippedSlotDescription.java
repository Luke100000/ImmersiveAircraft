package immersive_aircraft.entity.inventory.slots;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public abstract class TooltippedSlotDescription extends SlotDescription {
    public TooltippedSlotDescription(String type, int index, int x, int y, JsonObject json) {
        super(type, index, x, y, json);
    }

    public TooltippedSlotDescription(String type, RegistryFriendlyByteBuf buffer) {
        super(type, buffer);
    }

    public Optional<List<ClientTooltipComponent>> getToolTip() {
        FormattedCharSequence text = Component.translatable("immersive_aircraft.slot." + type().toLowerCase(Locale.ROOT)).getVisualOrderText();
        return Optional.of(List.of(ClientTooltipComponent.create(text)));
    }
}
