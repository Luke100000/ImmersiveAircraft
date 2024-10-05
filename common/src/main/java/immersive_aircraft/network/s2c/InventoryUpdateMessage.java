package immersive_aircraft.network.s2c;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class InventoryUpdateMessage extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryUpdateMessage> STREAM_CODEC = StreamCodec.ofMember(InventoryUpdateMessage::encode, InventoryUpdateMessage::new);
    public static final CustomPacketPayload.Type<InventoryUpdateMessage> TYPE = Message.createType("inventory_update");

    public CustomPacketPayload.Type<InventoryUpdateMessage> type() {
        return TYPE;
    }

    private final int vehicle;
    private final int index;
    private final Tag stack;

    public InventoryUpdateMessage(Entity entity, int index, ItemStack stack) {
        this.vehicle = entity.getId();
        this.index = index;
        if (stack.isEmpty()) {
            this.stack = null;
        } else {
            this.stack = stack.save(entity.registryAccess());
        }
    }

    public InventoryUpdateMessage(RegistryFriendlyByteBuf b) {
        vehicle = b.readInt();
        index = b.readInt();
        stack = b.readNbt();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf b) {
        b.writeInt(vehicle);
        b.writeInt(index);
        b.writeNbt(stack);
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

    public ItemStack getStack(Entity entity) {
        return this.stack == null ? ItemStack.EMPTY : ItemStack.parse(entity.registryAccess(), stack).orElse(ItemStack.EMPTY);
    }
}
