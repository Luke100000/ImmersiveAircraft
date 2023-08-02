package immersive_aircraft.network.s2c;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class InventoryUpdateMessage extends Message {
    private final int vehicle;
    private final int index;
    private final NbtCompound stack;

    public InventoryUpdateMessage(int id, int index, ItemStack stack) {
        this.vehicle = id;
        this.index = index;

        NbtCompound compound = new NbtCompound();
        stack.writeNbt(compound);
        this.stack = compound;
    }
    public InventoryUpdateMessage(PacketByteBuf b) {
        vehicle = b.readInt();
        index = b.readInt();
        stack = b.readNbt();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeInt(vehicle);
        b.writeInt(index);
        b.writeNbt(stack);
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handleInventoryUpdate(this);
    }

    public int getVehicle() {
        return vehicle;
    }

    public int getIndex() {
        return index;
    }

    public ItemStack getStack() {
        return ItemStack.fromNbt(stack);
    }

}
