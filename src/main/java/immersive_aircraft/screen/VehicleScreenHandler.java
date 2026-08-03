package immersive_aircraft.screen;

import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.item.UpgradeItem;
import immersive_aircraft.item.WeaponItem;
import immersive_aircraft.screen.slot.FuelSlot;
import immersive_aircraft.screen.slot.TypedSlot;
import immersive_aircraft.screen.slot.UpgradeSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;

public class VehicleScreenHandler extends Container {
    private final InventoryVehicleEntity vehicle;

    public VehicleScreenHandler(InventoryPlayer playerInventory, InventoryVehicleEntity vehicle) {
        this.vehicle = vehicle;

        vehicle.getInventory().openInventory(playerInventory.player);

        int titleHeight = 10;

        // Vehicle inventory
        for (VehicleInventoryDescription.Slot slot : this.vehicle.getInventoryDescription().getSlots()) {
            if (slot.type == VehicleInventoryDescription.SlotType.BOILER) {
                this.addSlotToContainer(new FuelSlot(vehicle.getInventory(), slot.index, slot.x, slot.y + titleHeight));
            } else if (slot.type == VehicleInventoryDescription.SlotType.WEAPON) {
                this.addSlotToContainer(new TypedSlot(WeaponItem.class, 1, vehicle.getInventory(), slot.index, slot.x, slot.y + titleHeight));
            } else if (slot.type == VehicleInventoryDescription.SlotType.UPGRADE) {
                this.addSlotToContainer(new UpgradeSlot(vehicle, UpgradeItem.class, 1, vehicle.getInventory(), slot.index, slot.x, slot.y + titleHeight));
            } else if (slot.type == VehicleInventoryDescription.SlotType.BOOSTER) {
                this.addSlotToContainer(new TypedSlot(ItemFirework.class, 64, vehicle.getInventory(), slot.index, slot.x, slot.y + titleHeight));
            } else if (slot.type == VehicleInventoryDescription.SlotType.BANNER) {
                this.addSlotToContainer(new TypedSlot(ItemBanner.class, 1, vehicle.getInventory(), slot.index, slot.x, slot.y + titleHeight));
            } else if (slot.type == VehicleInventoryDescription.SlotType.DYE) {
                this.addSlotToContainer(new TypedSlot(ItemDye.class, 1, vehicle.getInventory(), slot.index, slot.x, slot.y + titleHeight));
            } else {
                this.addSlotToContainer(new Slot(vehicle.getInventory(), slot.index, slot.x, slot.y + titleHeight));
            }
        }

        int h = this.vehicle.getInventoryDescription().getHeight() + titleHeight * 2;

        // The player inventory
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, h + y * 18));
            }
        }

        // The player Hotbar
        for (int x = 0; x < 9; ++x) {
            this.addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, h + 58));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return vehicle.getInventory().isUsableByPlayer(player) && vehicle.isEntityAlive() && vehicle.getDistance(player) < 8.0F;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        int inventorySize = vehicle.getInventory().getSizeInventory();
        if (slot != null && slot.getHasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (index < inventorySize) {
                if (!this.mergeItemStack(originalStack, inventorySize, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(originalStack, 0, inventorySize, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return newStack;
    }

    // Overwritten since max stack size isn't considered in vanilla
    @Override
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean inserted = false;

        // try to stack
        if (stack.isStackable()) {
            int i = startIndex;
            while (!stack.isEmpty() && (i < endIndex)) {
                Slot slot = this.inventorySlots.get(i);
                ItemStack target = slot.getStack();
                if (!target.isEmpty() && ItemStack.areItemsEqual(stack, target) && ItemStack.areItemStackTagsEqual(stack, target)) {
                    int diff = target.getCount() + stack.getCount();
                    int maxCount = slot.getItemStackLimit(stack);
                    if (diff <= maxCount) {
                        stack.setCount(0);
                        target.setCount(diff);
                        slot.onSlotChanged();
                        inserted = true;
                    } else if (target.getCount() < maxCount) {
                        stack.shrink(maxCount - target.getCount());
                        target.setCount(maxCount);
                        slot.onSlotChanged();
                        inserted = true;
                    }
                }
                i++;
            }
        }

        // use a new slot
        if (!stack.isEmpty()) {
            for (int i = startIndex; i < endIndex; i++) {
                Slot slot = this.inventorySlots.get(i);
                ItemStack target = slot.getStack();
                int maxCount = slot.getItemStackLimit(target);
                if (target.isEmpty() && slot.isItemValid(stack)) {
                    if (stack.getCount() > maxCount) {
                        slot.putStack(stack.splitStack(maxCount));
                    } else {
                        slot.putStack(stack.splitStack(stack.getCount()));
                    }
                    slot.onSlotChanged();
                    inserted = true;
                    break;
                }
            }
        }
        return inserted;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        this.vehicle.getInventory().closeInventory(player);
    }

    public InventoryVehicleEntity getVehicle() {
        return vehicle;
    }
}
