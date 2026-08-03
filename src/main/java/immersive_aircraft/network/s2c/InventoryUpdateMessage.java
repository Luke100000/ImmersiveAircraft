package immersive_aircraft.network.s2c;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class InventoryUpdateMessage extends Message {
    private int vehicle;
    private int index;
    private NBTTagCompound stack;

    public InventoryUpdateMessage() {
    }

    public InventoryUpdateMessage(int id, int index, ItemStack stack) {
        this.vehicle = id;
        this.index = index;

        NBTTagCompound compound = new NBTTagCompound();
        stack.writeToNBT(compound);
        this.stack = compound;
    }

    @Override
    protected void decode(PacketBuffer b) {
        vehicle = b.readInt();
        index = b.readInt();
        try {
            stack = b.readCompoundTag();
        } catch (java.io.IOException e) {
            stack = new NBTTagCompound();
        }
    }

    @Override
    public void encode(PacketBuffer b) {
        b.writeInt(vehicle);
        b.writeInt(index);
        b.writeCompoundTag(stack);
    }

    @Override
    public void receive(EntityPlayer e) {
        Main.networkManager.handleInventoryUpdate(this);
    }

    public int getVehicle() {
        return vehicle;
    }

    public int getIndex() {
        return index;
    }

    public ItemStack getStack() {
        return new ItemStack(stack);
    }
}
