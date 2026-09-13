package immersive_aircraft.network.s2c;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InventoryUpdateMessage extends Message {
    private final int vehicle;
    private final int index;
    private final ItemStack stack;

    public InventoryUpdateMessage(int id, int index, ItemStack stack) {
        this.vehicle = id;
        this.index = index;

        this.stack = stack;
    }

    public InventoryUpdateMessage(RegistryFriendlyByteBuf b) {
        vehicle = b.readInt();
        index = b.readInt();
        stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(b);
    }

    @Override
    public void encode(FriendlyByteBuf b) {
        b.writeInt(vehicle);
        b.writeInt(index);
        ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) b, stack);
    }

    @Override
    public void receive(Player e) {
        Main.networkManager.handleInventoryUpdate(this);
    }

    public int getVehicle() {
        return vehicle;
    }

    public int getIndex() {
        return index;
    }

    public ItemStack getStack() {
        return stack;
    }

}
