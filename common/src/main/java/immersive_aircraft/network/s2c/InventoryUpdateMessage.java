package immersive_aircraft.network.s2c;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InventoryUpdateMessage extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryUpdateMessage> STREAM_CODEC = StreamCodec.ofMember(InventoryUpdateMessage::encode, InventoryUpdateMessage::new);
    public static final CustomPacketPayload.Type<InventoryUpdateMessage> TYPE = Message.createType("inventory_update");

    public CustomPacketPayload.@NotNull Type<InventoryUpdateMessage> type() {
        return TYPE;
    }

    private final int vehicle;
    private final int index;
    private final ItemStack stack;

    public InventoryUpdateMessage(Entity entity, int index, ItemStack stack) {
        this.vehicle = entity.getId();
        this.index = index;
        this.stack = stack;
    }

    public InventoryUpdateMessage(RegistryFriendlyByteBuf b) {
        vehicle = b.readInt();
        index = b.readInt();
        boolean isEmpty = b.readBoolean();
        if (!isEmpty) {
            stack = b.readLenientJsonWithCodec(ItemStack.CODEC);
        } else {
            stack = ItemStack.EMPTY;
        }
    }

    @Override
    public void encode(RegistryFriendlyByteBuf b) {
        b.writeInt(vehicle);
        b.writeInt(index);
        b.writeBoolean(stack.isEmpty());
        if (!stack.isEmpty()) {
            b.writeJsonWithCodec(ItemStack.CODEC, stack);
        }
    }

    @Override
    public void receiveClient() {
        Main.messageHandler.handleInventoryUpdate(this);
    }

    public int getVehicle() {
        return vehicle;
    }

    public int getIndex() {
        return index;
    }

    public ItemStack getStack() {
        return this.stack;
    }
}
