package immersive_aircraft.entity.misc;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.c2s.RequestInventory;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

/**
 * 1.12.2 port: no SimpleInventory exists, so this is a plain NonNullList-backed IInventory
 * keeping the getStack/setStack/size call shape of the 1.16 code.
 */
public class SparseSimpleInventory implements IInventory {
    private final NonNullList<ItemStack> stacks;
    private final NonNullList<ItemStack> tracked;
    private boolean inventoryRequested = false;

    public SparseSimpleInventory(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        tracked = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public int size() {
        return stacks.size();
    }

    public ItemStack getStack(int index) {
        return getStackInSlot(index);
    }

    public void setStack(int index, ItemStack stack) {
        setInventorySlotContents(index, stack);
    }

    public NBTTagList writeNbt(NBTTagList nbtList) {
        for (int i = 0; i < this.size(); ++i) {
            if (this.getStack(i).isEmpty()) continue;
            NBTTagCompound nbtCompound = new NBTTagCompound();
            nbtCompound.setByte("Slot", (byte)i);
            this.getStack(i).writeToNBT(nbtCompound);
            nbtList.appendTag(nbtCompound);
        }
        return nbtList;
    }

    public void readNbt(NBTTagList nbtList) {
        this.clear();
        for (int i = 0; i < nbtList.tagCount(); ++i) {
            NBTTagCompound nbtCompound = nbtList.getCompoundTagAt(i);
            int slot = nbtCompound.getByte("Slot") & 0xFF;
            ItemStack itemStack = new ItemStack(nbtCompound);
            if (itemStack.isEmpty()) continue;
            this.setStack(slot, itemStack);
        }
    }

    private static boolean stacksEqual(ItemStack a, ItemStack b) {
        return a.getCount() == b.getCount() && ItemStack.areItemStacksEqual(a, b);
    }

    public void tick(InventoryVehicleEntity entity) {
        if (entity.world.isRemote) {
            // Sync initial inventory
            if (!inventoryRequested) {
                NetworkHandler.sendToServer(new RequestInventory(entity.getEntityId()));
                inventoryRequested = true;
            }
        } else {
            // Sync changed slots
            int index = entity.ticksExisted % entity.getInventoryDescription().getLastSyncIndex();
            ItemStack stack = getStack(index);
            ItemStack trackedStack = tracked.get(index);
            if (!stacksEqual(stack, trackedStack)) {
                tracked.set(index, stack.copy());
                for (EntityPlayer p : entity.world.playerEntities) {
                    // Players looking at the vehicle GUI get live updates through the container
                    if (!(p.openContainer instanceof VehicleScreenHandler && ((VehicleScreenHandler)p.openContainer).getVehicle() == entity)) {
                        if (p instanceof EntityPlayerMP) {
                            NetworkHandler.sendToPlayer(new InventoryUpdateMessage(entity.getEntityId(), index, stack), (EntityPlayerMP)p);
                        }
                    }
                }
            }
        }
    }

    // IInventory

    @Override
    public int getSizeInventory() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return stacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = stacks.get(index);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result;
        if (stack.getCount() <= count) {
            result = stack;
            stacks.set(index, ItemStack.EMPTY);
        } else {
            result = stack.splitStack(count);
        }
        markDirty();
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = stacks.get(index);
        stacks.set(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        stacks.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        stacks.clear();
    }

    @Override
    public String getName() {
        return "vehicle";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }
}
