package immersive_aircraft.network.s2c;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.network.SerializableNbt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class InventoryUpdateMessage implements Message {
    private final int vehicle;
    private final int index;
    private final SerializableNbt stack;

    public InventoryUpdateMessage(int id, int index, ItemStack stack) {
        this.vehicle = id;
        this.index = index;

        NbtCompound compound = new NbtCompound();
        stack.writeNbt(compound);
        this.stack = new SerializableNbt(compound);
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
        return ItemStack.fromNbt(stack.getNbt());
    }

}
