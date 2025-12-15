package immersive_aircraft.entity.inventory.slots;

import com.google.gson.JsonObject;
import immersive_aircraft.entity.InventoryVehicleEntity;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;
import java.util.Optional;

public class SlotDescription {
    protected final String type;
    protected final int index;
    protected final int x;
    protected final int y;

    public SlotDescription(String type, int index, int x, int y, JsonObject json) {
        this.type = type;
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public SlotDescription(String type, RegistryFriendlyByteBuf buffer) {
        this.type = type;
        this.index = buffer.readInt();
        this.x = buffer.readInt();
        this.y = buffer.readInt();
    }

    public Slot getSlot(InventoryVehicleEntity vehicle, Container inventory) {
        return new Slot(inventory, index, x, y);
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(index);
        buffer.writeInt(x);
        buffer.writeInt(y);
    }

    public Optional<List<ClientTooltipComponent>> getToolTip() {
        return Optional.empty();
    }

    public interface SlotDescriptionFactory {
        SlotDescription construct(String type, int index, int x, int y, JsonObject json);
    }

    public interface SlotDescriptionDecoder {
        SlotDescription decode(String type, RegistryFriendlyByteBuf buffer);
    }

    public String type() {
        return type;
    }

    public int index() {
        return index;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }
}
