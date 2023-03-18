package immersive_aircraft.network.s2c;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.*;

public class InventoryUpdateMessage implements Message {
    private final int vehicle;
    private final int index;
    private final Data stack;

    public InventoryUpdateMessage(int id, int index, ItemStack stack) {
        this.vehicle = id;
        this.index = index;

        NbtCompound compound = new NbtCompound();
        stack.writeNbt(compound);
        this.stack = new Data(compound);
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
        return ItemStack.fromNbt(stack.nbt);
    }

    private static final class Data implements Serializable {
        @Serial
        private static final long serialVersionUID = 5728742776742369248L;

        transient NbtCompound nbt;

        Data(NbtCompound nbt) {
            this.nbt = nbt;
        }

        @Serial
        private void writeObject(ObjectOutputStream out) throws IOException {
            NbtIo.write(nbt, out);
        }

        @Serial
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            nbt = NbtIo.read(in);
        }
    }
}
